package com.example.fitnessappnea

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
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
        println("workoutId: $workoutId")

        databaseHelper = DatabaseHelper(requireContext(), null)

        // Get the exercises needed to be completed
        exercises = databaseHelper.getWorkoutExercises(workoutId)
        val exerciseContainer = view.findViewById<LinearLayout>(R.id.exerciseContainer)

        for (exercise in exercises) {
            val exerciseTextView = TextView(requireContext())
            exerciseTextView.text = exercise.exerciseName
            exerciseTextView.textSize = 18f
            exerciseTextView.setPadding(0, 16, 0, 8)

            exerciseContainer.addView(exerciseTextView)


            for (setNumber in 1..exercise.sets) {
                val setRow = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 8, 0, 8)
                }

                val setTextView = TextView(requireContext())
                setTextView.text = "Set $setNumber"
                setTextView.textSize = 16f
                setTextView.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )

                val setCheckBox = CheckBox(requireContext())

                setRow.addView(setTextView)
                setRow.addView(setCheckBox)

                exerciseContainer.addView(setRow)
            }
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