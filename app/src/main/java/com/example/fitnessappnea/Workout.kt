package com.example.fitnessappnea

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.example.fitnessappnea.database.DatabaseHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.time.Duration

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Workout.newInstance] factory method to
 * create an instance of this fragment.
 */
class Workout : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_workout, container, false)
        fetchAllWorkouts(view)
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
                    println("Start Workout")
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }

    private fun fetchAllWorkouts(view: View) {
        val databaseHelper = DatabaseHelper(requireContext(), null)
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT WorkoutName, WorkoutDate, Duration, Notes FROM Workouts", null)

        if (cursor.moveToFirst()) {
            do {
                val WorkoutName = cursor.getString(cursor.getColumnIndex("WorkoutName"))
                val WorkoutDate = cursor.getString(cursor.getColumnIndex("WorkoutDate"))
                val Duration = cursor.getLong(cursor.getColumnIndex("Duration"))
                val Notes = cursor.getString(cursor.getColumnIndex("Notes"))

                addWorkoutToLayout(view, WorkoutName, WorkoutDate, Duration, Notes)
            } while (cursor.moveToNext())
        }

        cursor.close()
    }

    private fun addWorkoutToLayout(view: View, name: String, date: String, duration: Long, notes: String) {
        val workoutView = LayoutInflater.from(context).inflate(R.layout.workout_row, null)

        workoutView.findViewById<TextView>(R.id.workout_name).text = name // New TextView for Workout Name
        workoutView.findViewById<TextView>(R.id.workout_date).text = date
        workoutView.findViewById<TextView>(R.id.workout_duration).text = "Duration: $duration minutes"
        workoutView.findViewById<TextView>(R.id.workout_notes).text = notes

        val workoutList: LinearLayout? = view.findViewById(R.id.workout_list)
        workoutList?.addView(workoutView)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Workout.
         */
        // TODO: Rename and change types and number of parameters
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

