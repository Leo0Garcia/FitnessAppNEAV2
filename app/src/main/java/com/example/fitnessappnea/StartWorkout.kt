package com.example.fitnessapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.fitnessappnea.database.DatabaseHelper
import com.example.fitnessappnea.database.Exercise
import com.example.fitnessappnea.R
import com.example.fitnessappnea.database.Workout

class StartWorkout : Fragment() {

    // Declare variables
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start_workout, container, false)

        databaseHelper = DatabaseHelper(requireContext(), null)

        // Get workout details from arguments
        val workoutId = arguments?.getInt("workoutId") ?: 0
        val workoutName = arguments?.getString("workoutName") ?: ""
        val workoutList = arguments?.getParcelableArrayList<Workout>("workoutList") ?: mutableListOf()
        val workoutNameTextView = view.findViewById<TextView>(R.id.workout_name_view)
        workoutNameTextView.text = workoutName

        var exercises: List<Exercise>
        exercises = workoutList.find { it.workoutId == workoutId }?.exercises ?: emptyList()
        println(exercises)
        println("here")
        // Populate exercise list
        populateExercises(view, exercises)

        val finishButton = view.findViewById<Button>(R.id.finish_workout_button)
        finishButton.setOnClickListener {
            finishWorkout(workoutId, exercises)
        }

        return view
    }

    private fun populateExercises(view: View, exercises: List<Exercise>) {
        val exerciseContainer = view.findViewById<LinearLayout>(R.id.exerciseContainer)

        // Iterate through exercises and add them to the exercise container
        for (exercise in exercises) {
            val exerciseTextView = createTextView(exercise.exerciseName, 20f, R.color.textColour, true)
            detachViewIfNeeded(exerciseTextView)
            exerciseContainer.addView(exerciseTextView)

            val labelsRow = createLabelsRow()
            detachViewIfNeeded(labelsRow)
            exerciseContainer.addView(labelsRow)

            // Iterate through sets and append to the current exercise
            for (setNumber in 1..exercise.sets) {
                val setRow = createSetRow(exercise, setNumber)
                detachViewIfNeeded(setRow)
                exerciseContainer.addView(setRow)
            }

            // Add a spacer for formatting
            val spacerView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 48
                )
            }
            detachViewIfNeeded(spacerView)
            exerciseContainer.addView(spacerView)
        }
    }

    // Helper function to remove a view if it's already attached to a parent
    private fun detachViewIfNeeded(view: View) {
        val parent = view.parent as? ViewGroup
        parent?.removeView(view)
    }

    // Helper function to create a TextView
    private fun createTextView(text: String, textSize: Float, colorRes: Int, isBold: Boolean): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            this.textSize = textSize
            setTextColor(ContextCompat.getColor(requireContext(), colorRes))
            if (isBold) setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 16, 0, 8)
        }
    }

    // Add labels to the collumns for reps weight
    private fun createLabelsRow(): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 4)


            weightSum = 3f // Three collums total

            val spacer = createSpacer(1f)
            val spacer2 = createSpacer(1f)
            val repTextView = createTextView("Reps", 16f, R.color.setColour, true).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                gravity = android.view.Gravity.START
            }
            val weightTextView = createTextView("Weight", 16f, R.color.setColour, true).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                gravity = android.view.Gravity.START
            }

            // add everything to the view
            addView(spacer)
            addView(repTextView)
            addView(weightTextView)
            addView(spacer2)
        }
    }


    // create the spacer view for formatting
    private fun createSpacer(weight: Float): View {
        return TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
        }
    }

    // Create a set row for each set
    private fun createSetRow(exercise: Exercise, setNumber: Int): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)

            weightSum = 3f // 3 columns

            val setTextView = createTextView("$setNumber", 24f, R.color.setColour, true).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.5f
                ).apply {
                    setMargins(16, 0, 8, 0)
                }
            }

            // Container to limit width
            val inputContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    2.5f
                )
                setPadding(8, 0, 8, 0)
            }

            val repEditText = createEditableTextView("${exercise.reps}", 8).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.25f
                ).apply {
                    setMargins(0, 0, 16, 0)
                }
            }

            val weightEditText = createEditableTextView("${exercise.weight}", 16).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.25f
                )
            }

            // Add EditTexts to the container
            inputContainer.addView(repEditText)
            inputContainer.addView(weightEditText)

            val setCheckBox = createCheckBox().apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 0, 16, 0)
                }
            }

            // Add all views to the parent layout
            addView(setTextView)
            addView(inputContainer)
            addView(setCheckBox)

            // Store references for later data collection
            repEditText.tag = "rep-$exercise-${setNumber}"
            weightEditText.tag = "weight-$exercise-${setNumber}"
            setCheckBox.tag = "checkbox-$exercise-${setNumber}"
        }
    }

    // Subroutine to create editable text view (used for weight)
    private fun createEditableTextView(defaultValue: String, marginEnd: Int): EditText {
        return EditText(requireContext()).apply {
            setText(defaultValue);
            textSize = 20f;
            setTextColor(ContextCompat.getColor(requireContext(), R.color.textColour));
            setTypeface(null, android.graphics.Typeface.BOLD);
            background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_edittext_background);
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL;

            // Limit input length to 4 characters
            filters = arrayOf(android.text.InputFilter.LengthFilter(4));

            val bottom = paddingBottom;
            val top = paddingTop;
            setPadding(24, top, 24, bottom);

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(92, 0, 104, 0);
            };
        };
    }


    private fun createCheckBox(): CheckBox {
        return CheckBox(requireContext()).apply {
            buttonTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorAccent)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(32, 0, 16, 0)
            }
        }
    }

    private fun finishWorkout(workoutId: Int, exercises: List<Exercise>) {
        var completedExercises = mutableListOf<Exercise>()
        // Collect completed exercise data
        val parentLayout = view?.findViewById<LinearLayout>(R.id.exerciseContainer) ?: return
        for (exercise in exercises) {
            println(exercise.exerciseName)
            val completedExercise = Exercise(
                exerciseId = exercise.exerciseId,
                exerciseName = exercise.exerciseName,
                sets = exercise.sets,
                reps = exercise.reps,
                weight = exercise.weight,
                workoutId = workoutId
            )
            var totalReps = 0
            var totalWeight = 0.0
            // Fetch all inputted data fo each set
            for (setNumber in 1..exercise.sets) {
                // Use previously set tags to find views
                val repEditText = parentLayout.findViewWithTag<EditText>("rep-$exercise-$setNumber")
                val weightEditText = parentLayout.findViewWithTag<EditText>("weight-$exercise-$setNumber")
                val setCheckBox = parentLayout.findViewWithTag<CheckBox>("checkbox-$exercise-$setNumber")

                if (setCheckBox.isChecked) { // If exericse has been completed
                    val reps = repEditText.text.toString().toIntOrNull() ?: 0
                    val weight = weightEditText.text.toString().toDoubleOrNull() ?: 0.0
                    totalReps += reps
                    totalWeight += weight
                }
            }
            completedExercise.reps = totalReps
            completedExercise.weight = totalWeight
            completedExercises.add(completedExercise)
        }

        // Verify that at least one set is completed
        if (completedExercises.none { it.reps > 0 }) {
            Toast.makeText(requireContext(), "No sets completed", Toast.LENGTH_SHORT).show()
            return
        }

        println(completedExercises)

        // Save completed workout to the database
        databaseHelper.saveCompletedWorkout(completedExercises)

        Toast.makeText(requireContext(), "Workout completed and saved!", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack() // Navigate back using the stack
    }
}
