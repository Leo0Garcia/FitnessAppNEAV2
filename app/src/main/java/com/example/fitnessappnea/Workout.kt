package com.example.fitnessappnea

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import com.example.fitnessappnea.database.DatabaseHelper
import com.example.fitnessappnea.database.Exercise
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.w3c.dom.Text


class Workout : Fragment() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_workout, container, false)

        val workoutListLayout: LinearLayout = view.findViewById(R.id.workout_list)
        workoutListLayout.removeAllViews()

        val addStartButton: FloatingActionButton = view.findViewById(R.id.addStartWorkout)
        addStartButton.setOnClickListener {
            showPopup(addStartButton)
        }
        fetchAllCompletedWorkouts(view)
        return view
    }

    private fun showPopup(button: View) {
        val popupMenu = PopupMenu(requireContext(), button)
        popupMenu.menuInflater.inflate(R.menu.menu_workout_popup, popupMenu.menu)

        // Listener
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_new_workout -> {
                    // Go to AddWorkout frag
                    val fragment = AddWorkout()
                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.replace(R.id.frame_layout, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                    true
                }
                R.id.menu_start_workout -> {
                    // Go to StartWorkout frag
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

    private fun fetchAllCompletedWorkouts(view: View) {
        val exercises = mutableListOf<Exercise>()
        databaseHelper = DatabaseHelper(requireContext(), null)
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT completedId, workoutID, completionDate FROM CompletedWorkout", null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val completedID = cursor.getString(cursor.getColumnIndexOrThrow("completedId"))
                val workoutID = cursor.getString(cursor.getColumnIndexOrThrow("workoutId"))
                val completionDate = cursor.getString(cursor.getColumnIndexOrThrow("completionDate"))

                val cursorWorkout = db.rawQuery("SELECT workoutName FROM Workout WHERE workoutId = ?", arrayOf(workoutID))
                var setsCompleted = 0
                var repsCompleted = 0
                var weightUsed = 0.0


                // Fetch all the completed exercises
                val innerCursor = db.rawQuery("SELECT exerciseId, setsCompleted, repsCompleted, weightUsed FROM CompletedExercise WHERE completedId = ?", arrayOf(completedID))
                if (innerCursor.moveToFirst()) {
                    do {
                        val exerciseId = innerCursor.getString(innerCursor.getColumnIndexOrThrow("exerciseId"))
                        setsCompleted = innerCursor.getInt(innerCursor.getColumnIndexOrThrow("setsCompleted"))
                        repsCompleted = innerCursor.getInt(innerCursor.getColumnIndexOrThrow("repsCompleted"))
                        weightUsed = innerCursor.getDouble(innerCursor.getColumnIndexOrThrow("weightUsed"))
                        // Fetch the exercise name
                        val exerciseCursor = db.rawQuery("SELECT exerciseName FROM Exercise WHERE exerciseId = ?", arrayOf(exerciseId))
                        var exerciseName: String = ""
                        if (exerciseCursor.moveToFirst()) {
                            exerciseName =
                                exerciseCursor.getString(exerciseCursor.getColumnIndexOrThrow("exerciseName"))
                        }
                        exerciseCursor.close()
                        exercises.add(Exercise(exerciseId.toInt(), workoutID.toInt(), exerciseName, setsCompleted, repsCompleted, weightUsed))
                    } while (innerCursor.moveToNext())
                    innerCursor.close()
                }

                if (cursorWorkout.moveToFirst()) {
                    val workoutName = cursorWorkout.getString(cursorWorkout.getColumnIndexOrThrow("workoutName"))
                    addWorkoutToLayout(view, workoutName, completionDate, exercises)
                }

                cursorWorkout.close()
            } while (cursor.moveToNext())
        } else {
            println("No workouts found")
        }

        cursor?.close()
    }

    private fun addWorkoutToLayout(view: View, name: String, date: String, exercises: MutableList<Exercise>) {
        // Inflate the custom layout for each workout
        val workoutView = LayoutInflater.from(context).inflate(R.layout.workout_row, null)

        // Set data to the TextViews in the inflated layout
        workoutView.findViewById<TextView>(R.id.workout_name).text = name


        val exerciseContainer = workoutView.findViewById<LinearLayout>(R.id.exercise_container)
        exerciseContainer.removeAllViews()
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
}
