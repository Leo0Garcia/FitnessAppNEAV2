package com.example.fitnessappnea

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnessappnea.database.DatabaseHelper
import com.example.fitnessappnea.database.Exercise
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTests {

    private lateinit var context: Context
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var db: SQLiteDatabase

    @Before
    fun setup() {
        // Get application context to provide accurate behaviour
        context = ApplicationProvider.getApplicationContext()
        databaseHelper = DatabaseHelper(context, null)
        db = databaseHelper.writableDatabase

        // Clear the database before each test
        db.execSQL("DELETE FROM Workout")
        db.execSQL("DELETE FROM Exercise")
        db.execSQL("DELETE FROM CompletedWorkout")
        db.execSQL("DELETE FROM CompletedExercise")
        db.execSQL("DELETE FROM Nutrition")
        db.execSQL("DELETE FROM Sleep")
    }

    @After
    fun tearDown() {
        // Closes and deletes the database after testing has finished
        db.close()
        databaseHelper.close()

        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(DatabaseHelper.DATABASE_NAME)
    }

    @Test
    fun testGetAllWorkouts() {
        val workoutValues = ContentValues().apply { // Insert a test record
            put("workoutName", "Test Workout")
        }

        val workoutId = db.insert("Workout", null, workoutValues)
        assertTrue("Workout insert failed", workoutId != -1L)

        // Insert an exercise for the workout
        val exerciseValues = ContentValues().apply {
            put("workoutId", workoutId.toInt())
            put("exerciseName", "Test Exercise")
            put("sets", 3)
            put("reps", 10)
        }

        val exerciseId = db.insert("Exercise", null, exerciseValues)
        assertTrue("Exercise insert failed", exerciseId != -1L)

        // call getAllWorkouts() and check returned data
        val workouts = databaseHelper.getAllWorkouts()
        assertNotNull("No workouts returned", workouts)

        assertEquals("Unexpected number of workouts", 1, workouts.size)
        val workout = workouts.first()
        assertEquals("Unexpected workout name", "Test Workout", workout.workoutName)
        assertEquals("Unexpected number of exercises", 1, workout.exercises?.size)
        val exercise = workout.exercises?.first()
        assertEquals("Unexpected exercise name", "Test Exercise", exercise?.exerciseName)
        assertEquals("Unexpected number of sets", 3, exercise?.sets)
        assertEquals("Unexpected number of reps", 10, exercise?.reps)
    }

    @Test
    fun testFetchAllCompletedWorkouts() {
        val workoutValues = ContentValues().apply { // Insert a test record
            put("workoutName", "Test Workout")
        }

        val workoutId = db.insert("Workout", null, workoutValues)
        assertTrue("Workout insert failed", workoutId != -1L)

        // insert a row into the CompletedWorkout table
        val completedWorkoutSQLStatement = db.compileStatement("INSERT INTO Completedworkout (workoutId) VALUES (?)")
        completedWorkoutSQLStatement.bindLong(1, workoutId)
        val completedId = completedWorkoutSQLStatement.executeInsert()
        assertTrue("Completed workout insert failed", workoutId != -1L)

        // Insert an exercise for the workout
        val exerciseValues = ContentValues().apply {
            put("workoutId", workoutId.toInt())
            put("exerciseName", "Test Exercise")
            put("sets", 3)
            put("reps", 10)
            put("weight", 0.0)
        }
        val exerciseId = db.insert("Exercise", null, exerciseValues)
        assertTrue("Exercise insert failed", exerciseId != -1L)

        // insert a row into the CompletedExercise table
        val completedExerciseSQLStatement = db.compileStatement("INSERT INTO CompletedExercise (completedId, exerciseId, setsCompleted, repsCompleted, weightUsed) VALUES (?, ?, ?, ?, ?)")
        completedExerciseSQLStatement.bindLong(1, completedId)
        completedExerciseSQLStatement.bindLong(2, exerciseId)
        completedExerciseSQLStatement.bindLong(3, 3)
        completedExerciseSQLStatement.bindLong(4, 10)
        completedExerciseSQLStatement.bindDouble(5, 0.0)

        val completedExerciseId = completedExerciseSQLStatement.executeInsert()
        assertTrue("Completed exercise insert failed", completedExerciseId != -1L)

        // call fetchAllCompletedWorkouts() and check returned data
        val completedWorkouts = databaseHelper.fetchAllCompletedWorkouts()
        assertNotNull("No completed workouts returned", completedWorkouts)
        assertEquals("Unexpected number of completed workouts", 1, completedWorkouts.size)

        val completedWorkout = completedWorkouts.first()
        assertEquals("Unexpected workout name", "Test Workout", completedWorkout.workoutName)
        assertEquals("Unexpected number of exercises", 1, completedWorkout.exercises.size)

        val exercise = completedWorkout.exercises.first()
        assertEquals("Unexpected exercise name", "Test Exercise", exercise.exerciseName)
        assertEquals("Unexpected number of sets", 3, exercise.sets)
        assertEquals("Unexpected number of reps", 10, exercise.reps)
    }

    @Test
    fun testSaveCompletedWorkout() {
        val workoutValues = ContentValues().apply { // Insert a test record
            put("workoutName", "Test Workout")
        }

        val workoutId = db.insert("Workout", null, workoutValues)
        assertTrue("Workout insert failed", workoutId != -1L)

        // Insert an exercise for the workout
        val exerciseValues = ContentValues().apply {
            put("workoutId", workoutId.toInt())
            put("exerciseName", "Test Exercise")
            put("sets", 3)
            put("reps", 10)
            }

        val exerciseId = db.insert("Exercise", null, exerciseValues)
        assertTrue("Exercise insert failed", exerciseId != -1L)

        // Create a list of exercises for the completed workout
        val exercises = mutableListOf<Exercise>()
        exercises.add(
            Exercise(
                exerciseId.toInt(),
                workoutId.toInt(),
                "Test Exercise",
                3,
                10,
                0.0
            )
        )

        // Call saveCompletedWorkout() and pass through exemplar exercises
        databaseHelper.saveCompletedWorkout(exercises)

        // Fetch the completed workout from the database to verify values
        val completedWorkouts = databaseHelper.fetchAllCompletedWorkouts()
        assertNotNull("No completed workouts returned", completedWorkouts)
        assertEquals("Unexpected number of completed workouts", 1, completedWorkouts.size)
        val completedWorkout = completedWorkouts.first()
        assertEquals("Unexpected workout name", "Test Workout", completedWorkout.workoutName)
        assertEquals("Unexpected number of exercises", 1, completedWorkout.exercises.size)
        val exercise = completedWorkout.exercises.first()
        assertEquals("Unexpected exercise name", "Test Exercise", exercise.exerciseName)
        assertEquals("Unexpected number of sets", 3, exercise.sets)
        assertEquals("Unexpected number of reps", 10, exercise.reps)
    }

    @Test
    fun testSaveAndFetchNutritionData() {
        val testDate = "2025-02-06"

        // Insert test data into database
        val saveResult = databaseHelper.saveNutrition(
            date = testDate,
            protein = 20.0,
            carbohydrates = 20.0,
            fats = 15.0,
            fibre = 10.0,
            calories = 350.0,
            foodName = "Test Food"
        )

        assertTrue("Failed to save nutrition data", saveResult)

        // Retrieve test data from database
        val nutritionData = databaseHelper.getNutritionData(testDate)
        assertNotNull("Nutrition data not found", nutritionData)
        assertEquals("Unexpected protein value", 20.0, nutritionData.protein)
        assertEquals("Unexpected carbohydrates value", 20.0, nutritionData.carbohydrates)
        assertEquals("Unexpected fats value", 15.0, nutritionData.fats)
        assertEquals("Unexpected fibre value", 10.0, nutritionData.fibre)
        assertEquals("Unexpected calories value", 350.0, nutritionData.calories)

        // Retrieve list of food items
        val foodItems = databaseHelper.getFoodList(testDate)
        assertNotNull("Food items not found", foodItems)
        assertEquals("Unexpected number of food items", 1, foodItems.size)
        val foodData = foodItems.first()
        assertEquals("Unexpected food name", "Test Food", foodData.foodName)
        assertEquals("Unexpected protein value", 20.0, foodData.protein)
        assertEquals("Unexpected carbohydrates value", 20.0, foodData.carbohydrates)
        assertEquals("Unexpected fats value", 15.0, foodData.fats)
        assertEquals("Unexpected fibre value", 10.0, foodData.fibre)
        assertEquals("Unexpected calories value", 350.0, foodData.calories)
    }

    @Test
    fun testSaveAndFetchSleepData() {
        // Set test data
        val testDate = "2025-02-06"
        val sleepTime = "23:00"
        val wakeTime = "07:00"
        val sleepDuration: Int = 480

        // Save test data into database
        val saveResult = databaseHelper.saveSleepData(
            date = testDate,
            sleepTime = sleepTime,
            wakeTime = wakeTime,
            sleepDuration = sleepDuration.toDouble()
        )
        assertNotNull("Failed to save sleep data", saveResult)

        // Retrieve test data from database
        val sleepData = databaseHelper.getSleepData(testDate)
        assertNotNull("Sleep data not found", sleepData)
        assertEquals("Unexpected sleep time", sleepTime, sleepData?.sleepTime)
        assertEquals("Unexpected wake time", wakeTime, sleepData?.wakeTime)
        assertEquals("Unexpected sleep duration", sleepDuration, sleepData?.sleepDuration)


        // Verify sleep stage durations
        val expectedLight = (sleepDuration * 0.55).toInt()
        val expectedSWS = (sleepDuration * 0.20).toInt()
        val expectedREM = (sleepDuration * 0.25).toInt()
        assertEquals("Unexpected light duration", expectedLight, sleepData?.lightDuration)
        assertEquals("Unexpected SWS duration", expectedSWS, sleepData?.SWSDuration)
        assertEquals("Unexpected REM duration", expectedREM, sleepData?.REMDuration)
    }
}