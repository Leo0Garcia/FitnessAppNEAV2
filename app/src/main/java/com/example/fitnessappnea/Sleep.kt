package com.example.fitnessappnea

import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.fitnessappnea.database.DatabaseHelper
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Sleep : Fragment() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)

        databaseHelper = DatabaseHelper(requireContext(), null)

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val data = databaseHelper.getSleepData(currentDate)
//        if (data == null) {
//            showSleepTimeDialog()
//        }


        val barChart: BarChart = view.findViewById(R.id.sleepChart)

        // need to do

        return view
    }



    private fun showSleepTimeDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter Sleep & Wake Time")

        builder.setMessage("Please select your sleep and wake time.")

        builder.setPositiveButton("Set Time") { _, _ ->
            showTimePicker("Select Sleep Time") { sleepHour, sleepMinute ->
                showTimePicker("Select Wake Time") { wakeHour, wakeMinute ->
                    val sleepTime = String.format("%02d:%02d", sleepHour, sleepMinute)
                    val wakeTime = String.format("%02d:%02d", wakeHour, wakeMinute)

                    val sleepMinutes = sleepHour * 60 + sleepMinute
                    var wakeMinutes = wakeHour * 60 + wakeMinute

                    if (wakeMinutes < sleepMinutes) {
                        wakeMinutes += 24 * 60
                    }

                    val sleepDuration = wakeMinutes - sleepMinutes

                    Toast.makeText(requireContext(), "Sleep: $sleepTime, Wake: $wakeTime", Toast.LENGTH_LONG).show()
                    databaseHelper.saveSleepData(sleepTime, wakeTime, sleepDuration)
                }
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showTimePicker(title: String, onTimeSelected: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            onTimeSelected(selectedHour, selectedMinute)
        }, hour, minute, true).apply {
            setTitle(title)
            show()
        }
    }
}
