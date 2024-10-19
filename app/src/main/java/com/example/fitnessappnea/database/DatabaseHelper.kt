package com.example.fitnessappnea.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper



data class Workout(
    val workoutId: Int,
    val workoutName: String,
    val notes: String,
    val createdAt: String
)

class DatabaseHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {


    companion object {
        private const val DATABASE_NAME = "FitnessData.db"
        private const val DATABASE_VERSION = 11
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
    totalTime INTEGER,
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



            db?.execSQL(WorkoutQuery)
            db?.execSQL(ExerciseQuery)
            db?.execSQL(CompletedWorkoutQuery)
            db?.execSQL(CompletedExerciseQuery)
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
}
