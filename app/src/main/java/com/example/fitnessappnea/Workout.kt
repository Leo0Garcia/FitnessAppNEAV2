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
import com.example.fitnessappnea.database.DatabaseHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Workout : Fragment() {

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

        fetchAllCompletedWorkouts(view)

        val addStartButton: FloatingActionButton = view.findViewById(R.id.addStartWorkout)
        addStartButton.setOnClickListener {
            shopPopup(addStartButton)
        }
        return view
    }

    private fun shopPopup(button: View) {
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
        val databaseHelper = DatabaseHelper(requireContext(), null)
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT workoutID, completionDate, totalTime FROM CompletedWorkout", null)

        if (cursor.moveToFirst()) {
            do {
                val workoutID = cursor.getString(cursor.getColumnIndex("workoutID"))
                val completionDate = cursor.getString(cursor.getColumnIndex("completionDate"))
                val totalTime = cursor.getLong(cursor.getColumnIndex("totalTime"))

                val cursorWorkout = db.rawQuery("SELECT workoutName FROM Workout WHERE workoutId = ?", arrayOf(workoutID))

                if (cursorWorkout.moveToFirst()) {
                    val workoutName = cursorWorkout.getString(cursorWorkout.getColumnIndex("workoutName"))
                    addWorkoutToLayout(view, workoutName, completionDate, totalTime)
                }

                cursorWorkout.close() // Close inner cursor
            } while (cursor.moveToNext()) // TODO
        }

        cursor.close() // Close outer cursor
    }

    private fun addWorkoutToLayout(view: View, name: String, date: String, duration: Long) {
        // Inflate the custom layout for each workout
        val workoutView = LayoutInflater.from(context).inflate(R.layout.workout_row, null)

        // Set data to the TextViews in the inflated layout
        workoutView.findViewById<TextView>(R.id.workout_name).text = name
        workoutView.findViewById<TextView>(R.id.workout_date).text = date
        workoutView.findViewById<TextView>(R.id.workout_duration).text = "Duration: $duration minutes"

        // Add the inflated layout to the workout list
        val workoutList: LinearLayout = view.findViewById(R.id.workout_list)
        workoutList.addView(workoutView)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Workout().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
