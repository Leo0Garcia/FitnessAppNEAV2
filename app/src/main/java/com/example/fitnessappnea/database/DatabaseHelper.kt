package com.example.fitnessappnea.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Parcel
import android.os.Parcelable
import android.view.View


data class Workout(
    val workoutId: Int,
    val workoutName: String,
    val notes: String,
    val createdAt : String,
    val exercises: MutableList<Exercise>? = mutableListOf()
) : Parcelable { // Make Workout parcelable
    constructor(parcel: Parcel) : this( // How to handle parcelled data
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        mutableListOf<Exercise>().apply {
            parcel.readList(this, Exercise::class.java.classLoader)
        }
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) { // Convert all data into a parcel
        parcel.writeInt(workoutId)
        parcel.writeString(workoutName)
        parcel.writeString(notes)
        parcel.writeString(createdAt)
        parcel.writeList(exercises)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Workout> { // Methods for when parcel is needed
        override fun createFromParcel(parcel: Parcel): Workout {
            return Workout(parcel)
        }

        override fun newArray(size: Int): Array<Workout?> {
            return arrayOfNulls(size)
        }
    }
}

data class Exercise(
    val exerciseId: Int,
    val workoutId: Int,
    val exerciseName: String,
    val sets: Int,
    var reps: Int,
    var weight: Double
) : Parcelable { // Make Workout parcelable
    constructor(parcel: Parcel) : this( // How to handle parcelled data
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) { // Convert all data into a parcel
        parcel.writeInt(exerciseId)
        parcel.writeInt(workoutId)
        parcel.writeString(exerciseName)
        parcel.writeInt(sets)
        parcel.writeInt(reps)
        parcel.writeDouble(weight)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Exercise> { // Methods for when parcel is needed
        override fun createFromParcel(parcel: Parcel): Exercise {
            return Exercise(parcel)
        }

        override fun newArray(size: Int): Array<Exercise?> {
            return arrayOfNulls(size)
        }
    }
}

data class NutritionData( // Data class for foods/total nutritional data
    val protein: Double,
    val carbohydrates: Double,
    val fats: Double,
    val fibre: Double,
    val calories: Double,
    val foodName: String?
)

data class SleepData( // Data class for sleep data/avg data
    val wakeTime: String,
    val sleepTime: String,
    val sleepDuration: Int,
    val lightDuration: Int,
    val SWSDuration: Int,
    val REMDuration: Int
)

data class CompletedWorkout( // Data class for completed workouts
    val workoutName: String,
    val completionDate: String,
    val exercises: List<Exercise> // List of exercises completed in the workout
)


class DatabaseHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {


    companion object {
        const val DATABASE_NAME = "FitnessData.db"
        private const val DATABASE_VERSION = 20
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Setting up Tables
        try {
            val WorkoutQuery = """CREATE TABLE Workout (
    workoutId INTEGER PRIMARY KEY AUTOINCREMENT,
    workoutName TEXT NOT NULL CHECK(length(workoutName) > 0), -- Validate name isnt blank
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
);"""

            val ExerciseQuery = """CREATE TABLE Exercise (
    exerciseId INTEGER PRIMARY KEY AUTOINCREMENT,
    workoutId INTEGER NOT NULL,
    exerciseName TEXT NOT NULL CHECK(length(exerciseName) > 0),
    sets INTEGER NOT NULL CHECK(sets > 0),
    reps INTEGER NOT NULL CHECK(reps > 0),
    weight REAL CHECK(weight >= 0),
    FOREIGN KEY (workoutId) REFERENCES Workout(workoutId) ON DELETE CASCADE -- Delete child when parent is deleted
);"""

            val CompletedWorkoutQuery = """CREATE TABLE CompletedWorkout (
    completedId INTEGER PRIMARY KEY AUTOINCREMENT,
    workoutId INTEGER NOT NULL,
    completionDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (workoutId) REFERENCES Workout(workoutId) ON DELETE CASCADE
);"""

            val CompletedExerciseQuery = """CREATE TABLE CompletedExercise (
    completedExerciseId INTEGER PRIMARY KEY AUTOINCREMENT,
    completedId INTEGER NOT NULL,
    exerciseId INTEGER NOT NULL,
    setsCompleted INTEGER NOT NULL CHECK(setsCompleted > 0),
    repsCompleted INTEGER NOT NULL CHECK(repsCompleted > 0),
    weightUsed REAL CHECK(weightUsed >= 0),
    FOREIGN KEY (completedId) REFERENCES CompletedWorkout(completedId) ON DELETE CASCADE,
    FOREIGN KEY (exerciseId) REFERENCES Exercise(exerciseId) ON DELETE CASCADE
);"""

            val NutritionQuery = """CREATE TABLE Nutrition (
    uuid INTEGER PRIMARY KEY AUTOINCREMENT,
    date DATE DEFAULT CURRENT_DATE,
    protein REAL NOT NULL DEFAULT 0 CHECK(protein >= 0), -- Validation check for all nutritional data (cant be negative)
    carbohydrates REAL NOT NULL DEFAULT 0 CHECK(carbohydrates >= 0),
    fats REAL NOT NULL DEFAULT 0 CHECK(fats >= 0),
    fibre REAL NOT NULL DEFAULT 0 CHECK(fibre >= 0),
    calories REAL NOT NULL DEFAULT 0 CHECK(calories >= 0),
    foodName TEXT NOT NULL CHECK(length(foodName) > 0)
);"""

            val SleepQuery = """CREATE TABLE Sleep (
    uuid INTEGER PRIMARY KEY AUTOINCREMENT,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    wakeTime STRING NOT NULL,
    sleepTime STRING NOT NULL,
    sleepDuration INTEGER NOT NULL CHECK(sleepDuration > 0), -- Validation check for all sleep duration data
    lightDuration INTEGER NOT NULL CHECK(lightDuration >= 0), 
    SWSDuration INTEGER NOT NULL CHECK(SWSDuration >= 0), 
    REMDuration INTEGER NOT NULL CHECK(REMDuration >= 0),
    CHECK(lightDuration + SWSDuration + REMDuration = sleepDuration) -- Validation check there is no errors in calculations
);"""
            // 55% Light sleep
            // 20% SWS sleep
            // 25% REM Sleep
            // Using Whoop data and AI coach along with research

            db?.execSQL(WorkoutQuery)
            db?.execSQL(ExerciseQuery)
            db?.execSQL(CompletedWorkoutQuery)
            db?.execSQL(CompletedExerciseQuery)
            db?.execSQL(NutritionQuery)
            db?.execSQL(SleepQuery)

            // Add indexing for improved performance when filtering/joining/fetching data
            db?.execSQL("CREATE INDEX idx_exercise_workout ON Exercise(workoutId);")
            db?.execSQL("CREATE INDEX idx_completed_workout ON CompletedWorkout(workoutId);")
            db?.execSQL("CREATE INDEX idx_completed_exercise ON CompletedExercise(completedId, exerciseId);")
            db?.execSQL("CREATE INDEX idx_nutrition_date ON Nutrition(date);")
            db?.execSQL("CREATE INDEX idx_sleep_date ON Sleep(date);")

        } catch (e: Exception) { // If there is an error setting up database, print the stack trace
            e.printStackTrace()
            println("error")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) { // Drop all tables when updating DB
        db?.execSQL("DROP TABLE IF EXISTS Workout")
        db?.execSQL("DROP TABLE IF EXISTS Exercise")
        db?.execSQL("DROP TABLE IF EXISTS CompletedWorkout")
        db?.execSQL("DROP TABLE IF EXISTS CompletedExercise")
        db?.execSQL("DROP TABLE IF EXISTS Nutrition")
        db?.execSQL("DROP TABLE IF EXISTS Sleep")
        onCreate(db);
    }


    fun getAllWorkouts(): List<Workout> {
        val db = this.readableDatabase
        // Multi table query to fetch all data needed from both tables
        val cursor = db.rawQuery("""
SELECT 
    w.workoutId,
    w.workoutName,
    w.createdAt,
    e.exerciseId,
    e.exerciseName,
    e.sets,
    e.reps,
    e.weight
FROM Workout w
LEFT JOIN Exercise e ON w.workoutId = e.workoutId -- Join exercises to the related workout
ORDER BY w.workoutId, e.exerciseId; -- Order by workouts by workoutId and exercises by exerciseId

        """.trimIndent(), null)

        val workoutMap = mutableMapOf<Int, Workout>() // Map to store workouts by ID without duplicates

        while (cursor.moveToNext()) { // Iterate through each query result
            val workoutId = cursor.getInt(cursor.getColumnIndexOrThrow("workoutId"))

            // If workout is not already added, create it
            if (!workoutMap.containsKey(workoutId)) {
                workoutMap[workoutId] = Workout(
                    workoutId,
                    cursor.getString(cursor.getColumnIndexOrThrow("workoutName")) ?: "Unnamed Workout",
                    cursor.getString(cursor.getColumnIndexOrThrow("notes")) ?: "",
                    cursor.getString(cursor.getColumnIndexOrThrow("createdAt")) ?: "",
                    mutableListOf()
                )
            }

            // Check if there is an exercise linked to this workout for this entry in the results
            val exerciseId = cursor.getColumnIndex("exerciseId")
            val exercise = Exercise(
                exerciseId,
                workoutId,
                cursor.getString(cursor.getColumnIndexOrThrow("exerciseName")),
                cursor.getInt(cursor.getColumnIndexOrThrow("sets")),
                cursor.getInt(cursor.getColumnIndexOrThrow("reps")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("weight"))
            )
            workoutMap[workoutId]?.exercises?.add(exercise)
        }

        cursor.close()
        return workoutMap.values.toList()
    }

    fun fetchAllCompletedWorkouts(): List<CompletedWorkout> {
        val db = this.readableDatabase
        val completedWorkouts = mutableListOf<CompletedWorkout>()

        val query = """
        SELECT 
            cw.completedId, 
            cw.completionDate, 
            w.workoutName, 
            GROUP_CONCAT(e.exerciseId || '|' || e.exerciseName || '|' || ce.setsCompleted || '|' || ce.repsCompleted || '|' || ce.weightUsed) AS exercises
             -- Construct a concatenated string of exercises
        FROM CompletedWorkout cw
        JOIN Workout w ON cw.workoutId = w.workoutId
        LEFT JOIN CompletedExercise ce ON cw.completedId = ce.completedId -- Join completed exercises to the related workout
        LEFT JOIN Exercise e ON ce.exerciseId = e.exerciseId -- Join exercises to the related workout
        GROUP BY cw.completedId, w.workoutName
    """

        val cursor = db.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Extract data from the cursor
                val workoutName = cursor.getString(cursor.getColumnIndexOrThrow("workoutName"))
                val completionDate = cursor.getString(cursor.getColumnIndexOrThrow("completionDate"))
                val exercisesData = cursor.getString(cursor.getColumnIndexOrThrow("exercises"))

                val exercises = mutableListOf<Exercise>()

                // Parse exercises from the GROUP_CONCAT result
                exercisesData?.split(",")?.forEach { exerciseInfo ->
                    val parts = exerciseInfo.split("|")
                    if (parts.size == 5) {
                        val exerciseId = parts[0].toInt()
                        val exerciseName = parts[1]
                        val setsCompleted = parts[2].toInt()
                        val repsCompleted = parts[3].toInt()
                        val weightUsed = parts[4].toDouble()

                        // Create an Exercise object and add it to the list
                        exercises.add(Exercise(exerciseId, 0, exerciseName, setsCompleted, repsCompleted, weightUsed))
                    }
                }
                completedWorkouts.add(CompletedWorkout(workoutName, completionDate, exercises))
            } while (cursor.moveToNext())
        }

        cursor?.close()
        return completedWorkouts
    }

    fun saveCompletedWorkout(completedExercises: MutableList<Exercise>) {
        val db = this.writableDatabase

        val SQLQuery = "INSERT INTO CompletedWorkout (workoutId)VALUES (?)"
        val SQLStatement = db.compileStatement(SQLQuery)
        SQLStatement.bindLong(1, completedExercises[0].workoutId.toLong())

        // Execute insert and get the generated CompletedId
        val completedId = SQLStatement.executeInsert()

        if (completedId == -1L) {
            println("Error inserting workout")
            return
        }

        for (exercise in completedExercises) {
            // Insert each exercise into the CompletedExercise table with the related completedId
            val SQLQuery = "INSERT INTO CompletedExercise (completedId, exerciseId, setsCompleted, repsCompleted, weightUsed) VALUES (?, ?, ?, ?, ?)"
            val SQLStatement = db.compileStatement(SQLQuery)
            SQLStatement.bindLong(1, completedId) // Use the retrieved CompletedId
            SQLStatement.bindLong(2, exercise.exerciseId.toLong())
            SQLStatement.bindLong(3, exercise.sets.toLong())
            SQLStatement.bindLong(4, exercise.reps.toLong())
            SQLStatement.bindDouble(5, exercise.weight)
            SQLStatement.execute()
        }
    }


    fun saveNutrition(date: String, protein: Double, carbohydrates: Double, fats: Double, fibre: Double, calories: Double, foodName: String): Boolean {
        val db = this.writableDatabase
        // Insert data into Nutrition table
        val SQLQuery = "INSERT INTO Nutrition (date, protein, carbohydrates, fats, fibre, calories, foodName) VALUES (?, ?, ?, ?, ?, ?, ?)"
        val SQLStatement = db.compileStatement(SQLQuery)

        SQLStatement.bindString(1, date)
        SQLStatement.bindDouble(2, protein)
        SQLStatement.bindDouble(3, carbohydrates)
        SQLStatement.bindDouble(4, fats)
        SQLStatement.bindDouble(5, fibre)
        SQLStatement.bindDouble(6, calories)
        SQLStatement.bindString(7, foodName)

        SQLStatement.execute()
        return true
    }

    fun getNutritionData(date: String): NutritionData? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("""SELECT date, 
    SUM(protein) AS totalProtein, -- Sum up each macronutrient directly from all values in the table for that date
    SUM(carbohydrates) AS totalCarbs,
    SUM(fats) AS totalFats,
    SUM(fibre) AS totalFibre,
    SUM(calories) AS totalCalories
FROM Nutrition 
WHERE date = ?
GROUP BY date;
""".trimMargin(), arrayOf(date))

        if (cursor.moveToFirst()) { // Extract sum data
            val protein = cursor.getDouble(cursor.getColumnIndexOrThrow("totalProtein"))
            val carbohydrates = cursor.getDouble(cursor.getColumnIndexOrThrow("totalCarbs"))
            val fats = cursor.getDouble(cursor.getColumnIndexOrThrow("totalFats"))
            val fibre = cursor.getDouble(cursor.getColumnIndexOrThrow("totalFibre"))
            val calories = cursor.getDouble(cursor.getColumnIndexOrThrow("totalCalories"))
            return (NutritionData(protein, carbohydrates, fats, fibre, calories, null))
        }
        return NutritionData(0.0, 0.0, 0.0, 0.0, 0.0, null)
    }

    fun getFoodList(date: String): MutableList<NutritionData> {
        val db = this.readableDatabase
        // Fetch all data in Nutrition table under a given date
        val cursor = db.rawQuery(
            """
            SELECT *
            FROM Nutrition 
            WHERE date = ?""".trimMargin(), arrayOf(date)
        )

        val foodList = mutableListOf<NutritionData>()

        try {
            while (cursor.moveToNext()) { // Iterate through each food item for that day
                val data = NutritionData(
                    cursor.getDouble(cursor.getColumnIndexOrThrow("protein")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("carbohydrates")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("fats")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("fibre")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("calories")),
                    cursor.getString(cursor.getColumnIndexOrThrow("foodName"))
                )
                foodList.add(data)
            }
        } catch (e: Exception) {
            cursor.close()
            return foodList
        }
        return foodList
    }

    fun getSleepData(date: String): SleepData? {
        val db = this.readableDatabase
        // Fetch sleep data for a given date
        val cursor = db.rawQuery("""
            SELECT * FROM Sleep
            WHERE date = ?;
        """.trimIndent(), arrayOf(date))

        cursor.moveToFirst()
        if (cursor.count == 0) {
            return null // Indicate no sleep data for that date
        }
        val sleepData = SleepData(
                cursor.getString(cursor.getColumnIndexOrThrow("wakeTime")),
                cursor.getString(cursor.getColumnIndexOrThrow("sleepTime")),
                cursor.getInt(cursor.getColumnIndexOrThrow("sleepDuration")),
                cursor.getInt(cursor.getColumnIndexOrThrow("lightDuration")),
                cursor.getInt(cursor.getColumnIndexOrThrow("SWSDuration")),
                cursor.getInt(cursor.getColumnIndexOrThrow("REMDuration"))
            )
        cursor.close()

        return sleepData
    }

    fun get7DaySleepData(): List<SleepData> {
        val db = this.readableDatabase
        // Fetch sleep data for the last 7 days
        val cursor = db.rawQuery("""
            SELECT * FROM Sleep
            WHERE date >= DATE('now', '-7 days');
        """.trimIndent(), arrayOf())
        val sleepDataList = mutableListOf<SleepData>()
        while (cursor.moveToNext()) {
            val sleepData = SleepData(
                cursor.getString(cursor.getColumnIndexOrThrow("wakeTime")),
                cursor.getString(cursor.getColumnIndexOrThrow("sleepTime")),
                cursor.getInt(cursor.getColumnIndexOrThrow("sleepDuration")),
                cursor.getInt(cursor.getColumnIndexOrThrow("lightDuration")),
                cursor.getInt(cursor.getColumnIndexOrThrow("SWSDuration")),
                cursor.getInt(cursor.getColumnIndexOrThrow("REMDuration"))
            )
            sleepDataList.add(sleepData)
        }
        cursor.close()
        return sleepDataList
    }

    fun getAvgSleepData(date: String): SleepData? {
        val db = this.readableDatabase
        // Fetch average sleep data for a given date
        val cursor = db.rawQuery("""
            SELECT AVG(sleepDuration) AS avgSleep FROM Sleep -- Calculate average sleep duration for past 30 days
            WHERE date >= DATE('now', '-30 days');
        """.trimIndent(), arrayOf(date))

        cursor.moveToFirst()
        if (cursor.count == 0) {
            return null
        }
        val sleepData = SleepData(
            "",
            "",
            cursor.getInt(cursor.getColumnIndexOrThrow("avgSleep")),
            0,
            0,
            0
        )
        cursor.close()
        return sleepData
    }

    fun saveSleepData(date: String? = null, sleepTime: String, wakeTime: String, sleepDuration: Int) {
        // 55% Light sleep
        // 20% SWS sleep
        // 25% REM Sleep

        // Calculate duration of sleep stages
        val lightDuration = (sleepDuration * 0.55).toLong()
        val SWSDuration = (sleepDuration * 0.20).toLong()
        val REMDuration = (sleepDuration * 0.25).toLong()

        val db = this.writableDatabase
        // Insert all data (will be saved with the current date as default)
        val SQLQuery: String
        if (date == null) {
            SQLQuery = "INSERT INTO Sleep (wakeTime, sleepTime, sleepDuration, lightDuration, SWSDuration, REMDuration) VALUES (?, ?, ?, ?, ?, ?)"
        } else {
            SQLQuery = "INSERT INTO Sleep (date, wakeTime, sleepTime, sleepDuration, lightDuration, SWSDuration, REMDuration) VALUES (?, ?, ?, ?, ?, ?, ?)"
        }

        val SQLStatement = db.compileStatement(SQLQuery)

        SQLStatement.bindString(1, wakeTime)
        SQLStatement.bindString(2, sleepTime)
        SQLStatement.bindLong(3, sleepDuration.toLong())
        SQLStatement.bindLong(4, lightDuration)
        SQLStatement.bindLong(5, SWSDuration)
        SQLStatement.bindLong(6, REMDuration)
        SQLStatement.execute()
    }
}


