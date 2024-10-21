package com.example.fitnessappnea

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val workoutId = arguments?.getInt("workoutId") ?: 0 // Just incase value is empty or null set to 0
        println("workoutId: $workoutId")

        databaseHelper = DatabaseHelper(requireContext(), null)

        // Get the exercises needed to be completed
        exercises = databaseHelper.getWorkoutExercises(workoutId)
        println("exercises: $exercises")

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_workout, container, false)
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