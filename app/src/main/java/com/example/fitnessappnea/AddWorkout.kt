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
import com.example.fitnessappnea.database.Exercise
import java.text.SimpleDateFormat


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

        databaseHelper = DatabaseHelper(requireContext(), null) // Initialise databaseHelper

        // Find components from XML file
        workoutNameEditText = view.findViewById(R.id.workout_name)
        exerciseContainer = view.findViewById(R.id.exercise_container)


        // Setup addExercise button
        var addExerciseButton: Button = view.findViewById(R.id.add_exercise_button)
        addExerciseButton.setOnClickListener { // Define on click listener to add exercise container
            addExerciseRow()
        }

        // Setup saveWorkout button
        var saveWorkoutButton: Button = view.findViewById(R.id.save_workout_button)
        saveWorkoutButton.setOnClickListener { // Define on click listener to process and save workout data
            saveWorkout()
        }

        // Setup back button
        var backButton: Button = view.findViewById(R.id.back_to_workout_button)
        backButton.setOnClickListener { // Define on click listener to go back to workout list
            parentFragmentManager.popBackStack() // Pop the current page off of the stack to go back
        }

        return view
    }

    private fun addExerciseRow() {
        // Inflate each component into the container
        val exerciseLayout = LayoutInflater.from(context).inflate(R.layout.exercise_row, null)
        val exerciseEditText = exerciseLayout.findViewById<EditText>(R.id.exercise_name)
        val setsEditText = exerciseLayout.findViewById<EditText>(R.id.sets_input)
        val repsEditText = exerciseLayout.findViewById<EditText>(R.id.reps_input)

        exercises.add(Exercise(0, 0, "", 0, 0, 0.0)) // Placeholder entry

        exerciseContainer.addView(exerciseLayout)

        // Add TextWatchers to update exercise data
        exerciseEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val index = exerciseContainer.indexOfChild(exerciseLayout)
                exercises[index] = exercises[index].copy(exerciseName = s.toString()) // Update exercise name
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        setsEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val index = exerciseContainer.indexOfChild(exerciseLayout)
                val sets = s.toString().toIntOrNull() ?: 0 // default to 0
                exercises[index] = exercises[index].copy(sets = sets) // Update sets
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        repsEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val index = exerciseContainer.indexOfChild(exerciseLayout)
                val reps = s.toString().toIntOrNull() ?: 0
                exercises[index] = exercises[index].copy(reps = reps) // Update reps
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun saveWorkout() {
        val workoutName = workoutNameEditText.text.toString()

        if (workoutName.isEmpty()) { // Check the workout name isn't empty
            Toast.makeText(requireContext(), "Workout name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        databaseHelper.insertWorkout(workoutName, exercises) // Insert workout into database

        Toast.makeText(requireContext(), "Workout saved successfully!", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack() // Pop the current page off of the stack to go back
    }
}