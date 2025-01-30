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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class SelectWorkout : Fragment() {
    private lateinit var exerciseContainer: LinearLayout


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

        var backButton: Button = view.findViewById(R.id.back_to_workout_button)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Fetch all workouts
        var workoutList = databaseHelper.NEWgetAllWorkouts()


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

            button.setOnClickListener {
                startWorkout(workoutId, workoutName, workoutList)
            }
            workoutButtons.add(button)
        }

        exerciseContainer = view.findViewById(R.id.exercise_container)

        // Add buttons to the container
        for (button in workoutButtons) {
            exerciseContainer.addView(button)
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













    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StartWorkout.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SelectWorkout().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}