package com.example.fitnessappnea

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import com.example.fitnessappnea.database.CompletedWorkout
import com.example.fitnessappnea.database.DatabaseHelper
import com.example.fitnessappnea.database.Exercise
import com.google.android.material.floatingactionbutton.FloatingActionButton


class Workout : Fragment() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_workout, container, false)

        // Clear the workout list
        val workoutListLayout: LinearLayout = view.findViewById(R.id.workout_list)
        workoutListLayout.removeAllViews()

        // Add workout button and set onClick listener
        val addStartButton: FloatingActionButton = view.findViewById(R.id.addStartWorkout)
        addStartButton.setOnClickListener {
            showPopup(addStartButton)
        }
        // Initialise databaaseHelper class
        databaseHelper = DatabaseHelper(requireContext(), null)
        val completedWorkouts = databaseHelper.fetchAllCompletedWorkouts()
        val sortedCompletedWorkouts = mergeSortByDate(completedWorkouts)
        parseCompletedWorkouts(view, sortedCompletedWorkouts)
        return view
    }

    private fun showPopup(button: View) {
        // Create a popup menu when floating "+" button click
        val popupMenu = PopupMenu(requireContext(), button)
        popupMenu.menuInflater.inflate(R.menu.menu_workout_popup, popupMenu.menu)

        // Listen for menu item click
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_new_workout -> {
                    // Go to AddWorkout fragment
                    val fragment = AddWorkout()
                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.replace(R.id.frame_layout, fragment)
                    // Using a stack so using hardware back button goes to previous page by popping an item off the stack
                    transaction.addToBackStack(null)
                    transaction.commit()
                    true
                }
                R.id.menu_start_workout -> {
                    // Go to StartWorkout fragment
                    val fragment = SelectWorkout()
                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.replace(R.id.frame_layout, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun parseCompletedWorkouts(view: View, completedWorkouts: List<CompletedWorkout>) {
        for (workout in completedWorkouts) {
            addWorkoutToLayout(view, workout.workoutName, workout.completionDate, workout.exercises.toMutableList())
        }
    }

    private fun addWorkoutToLayout(view: View, name: String, date: String, exercises: MutableList<Exercise>) {
        // Inflate the custom layout for each workout
        val workoutView = LayoutInflater.from(context).inflate(R.layout.workout_row, null)

        workoutView.findViewById<TextView>(R.id.workout_name).text = name


        val exerciseContainer = workoutView.findViewById<LinearLayout>(R.id.exercise_container)
        exerciseContainer.removeAllViews()
        // Iterate through each exercise and add it to the exercise container
        for (exercise in exercises) {
            val exerciseTextView = TextView(exerciseContainer.context).apply {
                this.text = "${exercise.sets}x ${exercise.exerciseName}"
                this.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                this.setTextColor(resources.getColor(R.color.white))
            }
            exerciseContainer.addView(exerciseTextView)
        }

        workoutView.findViewById<TextView>(R.id.workout_date).text = date

        workoutView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            this.setMargins(0, 0, 0, 24)
        }

        // Add the inflated layout to the workout list
        val workoutList: LinearLayout = view.findViewById(R.id.workout_list)
        workoutList.addView(workoutView)
    }

    private fun mergeSortByDate(workouts: List<CompletedWorkout>): List<CompletedWorkout> {
        // 1 or 0 elements and the list doesnt need to be sorted
        if (workouts.size <= 1) {
            return workouts
        }

        val mid = workouts.size / 2
        val leftSorted = mergeSortByDate(workouts.subList(0, mid)) // Sort the left half using function recursion
        val rightSorted = mergeSortByDate(workouts.subList(mid, workouts.size)) // Sort the right half

        return merge(leftSorted, rightSorted) // Merge the sorted halves
    }

    private fun merge(left: List<CompletedWorkout>, right: List<CompletedWorkout>): List<CompletedWorkout> {
        var leftIndex = 0
        var rightIndex = 0
        val mergedList = mutableListOf<CompletedWorkout>()

        // Merge both sorted lists in descending order based on completion data.
        while (leftIndex < left.size && rightIndex < right.size) {
            if (left[leftIndex].completionDate >= right[rightIndex].completionDate) {
                mergedList.add(left[leftIndex])
                leftIndex++
            } else {
                mergedList.add(right[rightIndex])
                rightIndex++
            }
        }

        // Append any remaining elements.
        if (leftIndex < left.size) {
            mergedList.addAll(left.subList(leftIndex, left.size))
        }
        if (rightIndex < right.size) {
            mergedList.addAll(right.subList(rightIndex, right.size))
        }

        return mergedList
    }

}
