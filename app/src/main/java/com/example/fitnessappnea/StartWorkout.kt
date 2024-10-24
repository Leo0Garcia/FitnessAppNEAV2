package com.example.fitnessappnea

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.fitnessappnea.database.DatabaseHelper
import com.example.fitnessappnea.database.Exercise
import com.google.android.material.bottomnavigation.BottomNavigationView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER



class StartWorkout : Fragment() {

    private lateinit var exercises: List<Exercise>
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_start_workout, container, false)

        val workoutId = arguments?.getInt("workoutId") ?: 0 // Just incase value is empty or null set to 0
        val workoutName = arguments?.getString("workoutName") ?: ""
        val workoutNameTextView = view.findViewById<TextView>(R.id.workout_name_view)
        workoutNameTextView.text = workoutName

        println("workoutId: $workoutId")

        databaseHelper = DatabaseHelper(requireContext(), null)

        // Get the exercises needed to be completed
        exercises = databaseHelper.getWorkoutExercises(workoutId)
        val exerciseContainer = view.findViewById<LinearLayout>(R.id.exerciseContainer)

        for (exercise in exercises) {
            val exerciseTextView = TextView(requireContext())
            exerciseTextView.text = exercise.exerciseName
            exerciseTextView.textSize = 18f
            exerciseTextView.setTextColor(resources.getColor(R.color.textColour))
            exerciseTextView.setTypeface(null, android.graphics.Typeface.BOLD)
            exerciseTextView.setPadding(0, 16, 0, 8)

            exerciseContainer.addView(exerciseTextView)

            val labelsRow = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 4)

                val marginParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val marginInDp = -16 // shift left to align with values
                val marginInPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, marginInDp.toFloat(), resources.displayMetrics
                ).toInt()
                marginParams.setMargins(marginInPx, 0, 0, 0)
                layoutParams = marginParams
            }

            val spacer = TextView(requireContext())
            spacer.text = ""
            spacer.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

            val spacer2 = TextView(requireContext())
            spacer.text = ""
            spacer.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

            val repTextView = TextView(requireContext()).apply {
                text = "Reps"
                textSize = 12f
                setTextColor(resources.getColor(R.color.setColour))
                setTypeface(null, android.graphics.Typeface.BOLD)

                // Shifting to align with value
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    val marginInDp = -22
                    val marginInPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, marginInDp.toFloat(), resources.displayMetrics
                    ).toInt()
                    setMargins(marginInPx, 0, 0, 0)
                }
            }

            val weightTextView = TextView(requireContext()).apply {
                text = "Weight"
                textSize = 12f
                setTextColor(resources.getColor(R.color.setColour))
                setTypeface(null, android.graphics.Typeface.BOLD)

                // Shifting to align with value
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    val marginInDp = -36
                    val marginInPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, marginInDp.toFloat(), resources.displayMetrics
                    ).toInt()
                    setMargins(marginInPx, 0, 0, 0)
                }
            }

            labelsRow.addView(spacer)
            labelsRow.addView(repTextView)
            labelsRow.addView(weightTextView)
            labelsRow.addView(spacer2)

            exerciseContainer.addView(labelsRow)


            for (setNumber in 1..exercise.sets) {
                val setRow = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 16, 0, 16)
                }

                val setTextView = TextView(requireContext())
                setTextView.text = "$setNumber"
                setTextView.textSize = 24f
                setTextView.setTextColor(resources.getColor(R.color.setColour))
                setTextView.setTypeface(null, android.graphics.Typeface.BOLD)
                setTextView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 0, 142, 0)
                }

                val repEditText = EditText(requireContext()).apply {
                    val reps = exercise.reps
                    setText("$reps") // default value is weight previously set
                    textSize = 20f
                    setTextColor(resources.getColor(R.color.textColour))
                    setTypeface(null, android.graphics.Typeface.BOLD)

                    background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_edittext_background) // set background to shape to illustrate editable to user

                    val bottom = paddingBottom
                    val top = paddingTop
                    setPadding(24, top, 24, bottom)

                    // Set type
                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(92, 0, 104, 0)
                    }
                }

                val weightEditText = EditText(requireContext()).apply {
                    val weight = exercise.weight
                    setText("$weight") // default value is weight previously set
                    textSize = 20f
                    setTextColor(resources.getColor(R.color.textColour))
                    setTypeface(null, android.graphics.Typeface.BOLD)

                    background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_edittext_background) // set background to shape to illustrate editable to user

                    val bottom = paddingBottom
                    val top = paddingTop
                    setPadding(24, top, 24, bottom)

                    // Set type
                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(104, 0, 104, 0)
                    }
                }


                val setCheckBox = CheckBox(requireContext()).apply {
                    buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent))
                }

                setCheckBox.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(32, 0, 16, 0)
                }

                setRow.addView(setTextView)
                setRow.addView(repEditText)
                setRow.addView(weightEditText)
                setRow.addView(setCheckBox)

                exerciseContainer.addView(setRow)
            }
            // create space between each exercise
            val spacerView = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    48
                )
            }

            exerciseContainer.addView(spacerView)
        }



        return view
    }





    companion object {
        @JvmStatic
        fun newInstance(workoutId: Int) =
            StartWorkout().apply {
                arguments = Bundle().apply {
                    putInt("workoutId", workoutId)
                }
            }
    }
}