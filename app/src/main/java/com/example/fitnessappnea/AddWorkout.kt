package com.example.fitnessappnea

import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Layout
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.fitnessappnea.database.DatabaseHelper
import java.text.SimpleDateFormat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

data class Exercise(
    val name: String,
    val sets: Int,
    val reps: Int
)
data class WorkoutContainer(
    val name: String,
    val exercises: List<Exercise>
)


/**
 * A simple [Fragment] subclass.
 * Use the [AddWorkout.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddWorkout : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var exerciseContainer: LinearLayout
    private lateinit var workoutNameEditText: EditText
    private val exercises = mutableListOf<Exercise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_add_workout, container, false)

        databaseHelper = DatabaseHelper(requireContext(), null)

        workoutNameEditText = view.findViewById(R.id.workout_name)
        exerciseContainer = view.findViewById(R.id.exercise_container)


        var addExerciseButton: Button = view.findViewById(R.id.add_exercise_button)
        addExerciseButton.setOnClickListener {
            addExerciseRow()
        }

        var saveWorkoutButton: Button = view.findViewById(R.id.save_workout_button)
        saveWorkoutButton.setOnClickListener {
            saveWorkout()
        }

        var backButton: Button = view.findViewById(R.id.back_to_workout_button)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

//        var backButton: Button = view.findViewById(R.id.back_button)
//        backButton.setOnClickListener {
//            parentFragmentManager.popBackStack()
//        }

        return view
    }

    private fun addExerciseRow() {
        val exerciseLayout = LayoutInflater.from(context).inflate(R.layout.exercise_row, null)
        val exerciseEditText = exerciseLayout.findViewById<EditText>(R.id.exercise_name)

        exercises.add(Exercise("", 0, 0))
        println("Here")

        exerciseContainer.addView(exerciseLayout)

        exerciseEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val index = exerciseContainer.indexOfChild(exerciseLayout)
                exercises[index] = Exercise(s.toString(), 0, 0) // Update the exercise name
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed
            }
        })
    }

    private fun saveWorkout() {
        val workoutName = workoutNameEditText.text.toString()
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val workoutDate = formatter.format(Calendar.getInstance().time)
        val duration = null
        val notes = "null"

        val dbHelper = DatabaseHelper(requireContext(), null)

        val db = dbHelper.writableDatabase
        db.beginTransaction()

        //println(db.execSQL("SELECT * FROM Workouts"))

        try {
            // Insert the workout
            val workoutQuery = "INSERT INTO Workouts (WorkoutName, WorkoutDate, Duration, Notes) VALUES (?,?,?,?);".trimIndent()
            val workoutStatement = db.compileStatement(workoutQuery)
            workoutStatement.bindString(1, workoutName)
            workoutStatement.bindString(2, workoutDate)
            workoutStatement.bindNull(3)
            workoutStatement.bindString(4, notes)

            val workoutID = workoutStatement.executeInsert()

            // Insert each exercise
            for (exercise in exercises) {
                val exerciseQuery = "INSERT INTO Exercises (WorkoutID, ExerciseName, Sets, Reps, Weight, Duration, Notes) VALUES (?,?,?,?,?,?,?)".trimIndent()
                val exerciseStatement = db.compileStatement(exerciseQuery)
                exerciseStatement.bindLong(1, workoutID)
                exerciseStatement.bindString(2, exercise.name)
                exerciseStatement.bindLong(3, exercise.sets.toLong())
                exerciseStatement.bindLong(4, exercise.reps.toLong())
                exerciseStatement.bindDouble(5, 0.0)
                exerciseStatement.bindLong(6, 0)
                exerciseStatement.bindString(7, "null")

                exerciseStatement.executeInsert()
            }

            db.setTransactionSuccessful()

            Toast.makeText(requireContext(), "Workout saved Successfully!", Toast.LENGTH_SHORT).show()

            parentFragmentManager.popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
        println("MAN DID DAT TING")

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddWorkout.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddWorkout().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}