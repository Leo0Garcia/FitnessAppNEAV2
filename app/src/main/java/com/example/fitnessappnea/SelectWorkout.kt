package com.example.fitnessappnea

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.example.fitnessapp.StartWorkout
import com.example.fitnessappnea.database.DatabaseHelper
import com.example.fitnessappnea.database.Workout


class SelectWorkout : Fragment() {
    private lateinit var workoutContainer: LinearLayout


    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view =  inflater.inflate(R.layout.fragment_select_workout, container, false)

        databaseHelper = DatabaseHelper(requireContext(), null)

        // Set back button to go back a page
        var backButton: Button = view.findViewById(R.id.back_to_workout_button)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack() // Use the stack to pop the current fragment off to go back a page
        }

        // Fetch all workouts
        var workoutList = databaseHelper.getAllWorkouts()

        // Create a list of workout names and ids for use when starting the workout
        var workouts = mutableListOf<Pair<Int, String>>()
        for (workout in workoutList) {
            workouts.add(Pair(workout.workoutId, workout.workoutName))
        }

        var workoutButtons = mutableListOf<Button>()

        val marginInDp = 12
        val marginInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, marginInDp.toFloat(), resources.displayMetrics
        ).toInt()

        // Create buttons for each workout
        for ((workoutId, workoutName) in workouts) {
            var button = Button(requireContext())

            // Formatting for button
            button.text = workoutName
            button.background = resources.getDrawable(R.drawable.button_rounded)
            button.setTextColor(resources.getColor(R.color.colorAccent))
            button.setTypeface(null, Typeface.BOLD)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, marginInPx, 0, 0) // Only set top margin
            button.layoutParams = layoutParams

            // Add arrow icon
            val arrowIcon = resources.getDrawable(R.drawable.baseline_arrow_forward_24, null)
            button.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowIcon, null)
            button.compoundDrawablePadding = 16

            button.setOnClickListener { // When button is clicked, start the workout selected
                startWorkout(workoutId, workoutName, workoutList)
            }
            workoutButtons.add(button)
        }

        workoutContainer = view.findViewById(R.id.workout_container)

        // Add buttons to the container
        for (button in workoutButtons) {
            workoutContainer.addView(button)
        }

        return view
    }


    private fun startWorkout(workoutId: Int, workoutName: String, workoutList: List<Workout>) {
        val fragment = StartWorkout()

        val bundle = Bundle().apply {
            putInt("workoutId", workoutId)
            putString("workoutName", workoutName)
            putParcelableArrayList("workoutList", ArrayList(workoutList))
        }

        fragment.arguments = bundle

        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}