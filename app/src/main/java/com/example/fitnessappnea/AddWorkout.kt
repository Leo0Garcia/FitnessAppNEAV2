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

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private data class Exercise(
    val name: String,
    val sets: Int,
    val reps: Int
)
//data class WorkoutContainer(
//    val name: String,
//    val exercises: List<Exercise>
//)


class AddWorkout : Fragment() {
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
        val setsEditText = exerciseLayout.findViewById<EditText>(R.id.sets_input)
        val repsEditText = exerciseLayout.findViewById<EditText>(R.id.reps_input)

        exercises.add(Exercise("", 0, 0)) // Placeholder entry

        exerciseContainer.addView(exerciseLayout)

        // Add TextWatchers to update exercise data
        exerciseEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val index = exerciseContainer.indexOfChild(exerciseLayout)
                exercises[index] = exercises[index].copy(name = s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        setsEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val index = exerciseContainer.indexOfChild(exerciseLayout)
                val sets = s.toString().toIntOrNull() ?: 0 // default to 0
                exercises[index] = exercises[index].copy(sets = sets)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        repsEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val index = exerciseContainer.indexOfChild(exerciseLayout)
                val reps = s.toString().toIntOrNull() ?: 0
                exercises[index] = exercises[index].copy(reps = reps)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun saveWorkout() {
        val workoutName = workoutNameEditText.text.toString()
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val workoutDate = formatter.format(Calendar.getInstance().time)

        val db = databaseHelper.writableDatabase
        db.beginTransaction()

        try {
            // Insert the workout
            val workoutQuery = "INSERT INTO Workout (workoutName) VALUES (?);"
            val workoutStatement = db.compileStatement(workoutQuery)
            workoutStatement.bindString(1, workoutName)
            val workoutID = workoutStatement.executeInsert()

            // Insert each exercise
            for (exercise in exercises) {
                val exerciseQuery = "INSERT INTO Exercise (workoutId, exerciseName, sets, reps) VALUES (?,?,?,?)"
                val exerciseStatement = db.compileStatement(exerciseQuery)
                exerciseStatement.bindLong(1, workoutID)
                exerciseStatement.bindString(2, exercise.name)
                exerciseStatement.bindLong(3, exercise.sets.toLong())
                exerciseStatement.bindLong(4, exercise.reps.toLong())

                exerciseStatement.executeInsert()
            }

            db.setTransactionSuccessful()

            Toast.makeText(requireContext(), "Workout saved successfully!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
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