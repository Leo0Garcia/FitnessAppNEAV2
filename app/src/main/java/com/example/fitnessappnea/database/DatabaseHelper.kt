package com.example.fitnessappnea.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQuery
import android.view.View


data class Workout(
    val workoutId: Int,
    val workoutName: String,
    val notes: String,
    val createdAt: String
)

data class Exercise(
    val exerciseId: Int,
    val workoutId: Int,
    val exerciseName: String,
    val sets: Int,
    var reps: Int,
    var weight: Double
)

data class NutritionData(
    val protein: Double,
    val carbohydrates: Double,
    val fats: Double,
    val fibre: Double,
    val calories: Double
)

class DatabaseHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {


    companion object {
        private const val DATABASE_NAME = "FitnessData.db"
        private const val DATABASE_VERSION = 13
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Setting up Tables
        try {
            // Old Schema
//            val WorkoutQuery = "CREATE TABLE Workouts (WorkoutID INTEGER PRIMARY KEY AUTOINCREMENT, WorkoutName VARCHAR(255) NOT NULL, WorkoutDate DATE, Duration INT, Notes TEXT, CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
//            val ExercisesQuery = "CREATE TABLE Exercises (ExerciseID INTEGER PRIMARY KEY AUTOINCREMENT, WorkoutID INT NOT NULL, ExerciseName VARCHAR(255) NOT NULL, Sets INT NOT NULL, Reps INT, Weight Decimal(5,2), Duration INT, Notes TEXT, FOREIGN KEY (WorkoutID) REFERENCES Workouts(WorkoutID))"

            // New Schema
            val WorkoutQuery = """CREATE TABLE Workout (
    workoutId INTEGER PRIMARY KEY AUTOINCREMENT,
    workoutName TEXT NOT NULL,
    notes TEXT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)"""

            val ExerciseQuery = """CREATE TABLE Exercise (
    exerciseId INTEGER PRIMARY KEY AUTOINCREMENT,
    workoutId INTEGER NOT NULL,
    exerciseName TEXT NOT NULL,
    sets INTEGER NOT NULL,
    reps INTEGER NOT NULL,
    weight REAL,
    FOREIGN KEY (workoutId) REFERENCES Workout(workoutId)
)"""

            val CompletedWorkoutQuery = """CREATE TABLE CompletedWorkout (
    completedId INTEGER PRIMARY KEY AUTOINCREMENT,
    workoutId INTEGER NOT NULL,
    completionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (workoutId) REFERENCES Workout(workoutId)
)"""

            val CompletedExerciseQuery = """CREATE TABLE CompletedExercise (
    completedExerciseId INTEGER PRIMARY KEY AUTOINCREMENT,
    completedId INTEGER NOT NULL,
    exerciseId INTEGER NOT NULL,
    setsCompleted INTEGER NOT NULL,
    repsCompleted INTEGER NOT NULL,
    weightUsed REAL,
    FOREIGN KEY (completedId) REFERENCES CompletedWorkout(completedId),
    FOREIGN KEY (exerciseId) REFERENCES Exercise(exerciseId)
)"""

            val NutritionQuery = """CREATE TABLE Nutrition (
    date DATE PRIMARY KEY,
    protein REAL,
    carbohydrates REAL,
    fats REAL,
    fibre REAL,
    calories REAL
)"""

            db?.execSQL(WorkoutQuery)
            db?.execSQL(ExerciseQuery)
            db?.execSQL(CompletedWorkoutQuery)
            db?.execSQL(CompletedExerciseQuery)
            db?.execSQL(NutritionQuery)

        } catch (e: Exception) {
            e.printStackTrace()
            println("error")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Workout")
        db?.execSQL("DROP TABLE IF EXISTS Exercise")
        db?.execSQL("DROP TABLE IF EXISTS CompletedWorkout")
        db?.execSQL("DROP TABLE IF EXISTS CompletedExercise")
        onCreate(db);
    }

    fun getAllWorkouts(): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Workout", null)
        while (cursor.moveToNext()) {
            var workout: Workout = Workout(
                cursor.getInt(cursor.getColumnIndexOrThrow("workoutId")), // Throw an error if the column doesn't exist
                cursor.getString(cursor.getColumnIndexOrThrow("workoutName"))?: "ERROR: WORKOUT NAME NULL",
                cursor.getString(cursor.getColumnIndexOrThrow("notes"))?: "",
                cursor.getString(cursor.getColumnIndexOrThrow("createdAt"))?: ""
            )
            workouts.add(workout)
        }

        cursor.close()
        return workouts
    }

    fun getWorkoutExercises(workoutId: Int): List<Exercise> {
        val exercises = mutableListOf<Exercise>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Exercise WHERE workoutId = $workoutId", null)
        while (cursor.moveToNext()) {
            var exercise: Exercise = Exercise(
                cursor.getInt(cursor.getColumnIndexOrThrow("exerciseId")), // Throw an error if the column doesn't exist
                cursor.getInt(cursor.getColumnIndexOrThrow("workoutId")),
                cursor.getString(cursor.getColumnIndexOrThrow("exerciseName")),
                cursor.getInt(cursor.getColumnIndexOrThrow("sets")),
                cursor.getInt(cursor.getColumnIndexOrThrow("reps")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("weight"))
            )

            exercises.add(exercise)
        }
        cursor.close()
        return exercises
    }

    fun saveExercise(exerciseId: Int, reps: Int, weight: Double): Boolean {
        val db = this.writableDatabase
        val SQLQuery = "UPDATE Exercise SET reps = ?, weight = ? WHERE exerciseId = ?"
        val SQLStatement = db.compileStatement(SQLQuery)
        SQLStatement.bindLong(1, reps.toLong())
        SQLStatement.bindDouble(2, weight)
        SQLStatement.bindLong(3, exerciseId.toLong())
        SQLStatement.execute()
        db.close()
        return true

    }

    fun saveCompletedWorkout(completedExercises: MutableList<Exercise>) {
        val db = this.writableDatabase
        println(completedExercises)

        val SQLQuery = "INSERT INTO CompletedWorkout (workoutId) VALUES (?)"
        val SQLStatement = db.compileStatement(SQLQuery)
        SQLStatement.bindLong(1, completedExercises[0].workoutId.toLong())
        val data = SQLStatement.execute()

        for (exercise in completedExercises) {
            val SQLQuery = "INSERT INTO CompletedExercise (completedId, exerciseId, setsCompleted, repsCompleted, weightUsed) VALUES (?, ?, ?, ?, ?)"
            val SQLStatement = db.compileStatement(SQLQuery)
            SQLStatement.bindLong(1, exercise.workoutId.toLong())
            SQLStatement.bindLong(2, exercise.exerciseId.toLong())
            SQLStatement.bindLong(3, exercise.sets.toLong())
            SQLStatement.bindLong(4, exercise.reps.toLong())
            SQLStatement.bindDouble(5, exercise.weight)
            SQLStatement.execute()
        }

    }

    fun saveNutrition(date: String, protein: Double, carbohydrates: Double, fats: Double, fibre: Double, calories: Double): Boolean {
        val db = this.writableDatabase

        // Need to update the record if it already exists, adding to the totals or if it doesnt exist then make the record
        val cursor = db.rawQuery("SELECT * FROM Nutrition WHERE date = ?", arrayOf(date))
        if (cursor.moveToFirst()) {
            val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            val protein = cursor.getDouble(cursor.getColumnIndexOrThrow("protein")) + protein
            val carbohydrates = cursor.getDouble(cursor.getColumnIndexOrThrow("carbohydrates")) + carbohydrates
            val fats = cursor.getDouble(cursor.getColumnIndexOrThrow("fats")) + fats
            val fibre = cursor.getDouble(cursor.getColumnIndexOrThrow("fibre")) + fibre
            val calories = cursor.getDouble(cursor.getColumnIndexOrThrow("calories")) + calories

            val SQLQuery = "UPDATE Nutrition SET protein = ?, carbohydrates = ?, fats = ?, fibre = ?, calories = ? WHERE date = ?"
            val SQLStatement = db.compileStatement(SQLQuery)
            SQLStatement.bindDouble(1, protein)
            SQLStatement.bindDouble(2, carbohydrates)
            SQLStatement.bindDouble(3, fats)
            SQLStatement.bindDouble(4, fibre)
            SQLStatement.bindDouble(5, calories)
            SQLStatement.bindString(6, date)
            SQLStatement.execute()
        } else {
            val SQLQuery = "INSERT INTO Nutrition (date, protein, carbohydrates, fats, fibre, calories) VALUES (?, ?, ?, ?, ?, ?)"
            val SQLStatement = db.compileStatement(SQLQuery)

            SQLStatement.bindString(1, date)
            SQLStatement.bindDouble(2, protein)
            SQLStatement.bindDouble(3, carbohydrates)
            SQLStatement.bindDouble(4, fats)
            SQLStatement.bindDouble(5, fibre)
            SQLStatement.bindDouble(6, calories)
            SQLStatement.execute()
        }

        cursor.close()

        return true
    }

    fun getNutritionData(date: String): NutritionData? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Nutrition WHERE date = ?", arrayOf(date))

        if (cursor.moveToFirst()) {
            val protein = cursor.getDouble(cursor.getColumnIndexOrThrow("protein"))
            val carbohydrates = cursor.getDouble(cursor.getColumnIndexOrThrow("carbohydrates"))
            val fats = cursor.getDouble(cursor.getColumnIndexOrThrow("fats"))
            val fibre = cursor.getDouble(cursor.getColumnIndexOrThrow("fibre"))
            val calories = cursor.getDouble(cursor.getColumnIndexOrThrow("calories"))

            cursor.close()

            println("Protein: $protein, Carbohydrates: $carbohydrates, Fats: $fats, Fibre: $fibre, Calories: $calories")

            return NutritionData(protein, carbohydrates, fats, fibre, calories)
        }

        cursor.close()
        return null
    }
}


