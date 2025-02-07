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
import com.example.fitnessappnea.database.SleepData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
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
            showSleepTimeDialog()
//        }


        val chart: LineChart = view.findViewById(R.id.sleepChart)
        val weekData = databaseHelper.get7DaySleepData()

        println(weekData)

        if (data != null) {
            displayChart(chart, weekData)
        }

        return view
    }


    private fun displayChart(chart: LineChart, data: List<SleepData>) {
        val sleepEntries = mutableListOf<Entry>()
        val wakeEntries = mutableListOf<Entry>()
        var index = 0

        for (item in data) {
            val sleepTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(item.sleepTime)
            val wakeTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(item.wakeTime)

            if (sleepTime != null && wakeTime != null) {
                println(sleepTime.time.toFloat())
                println(wakeTime.time.toFloat())
                sleepEntries.add(Entry(index.toFloat(), sleepTime.time.toFloat()))
                wakeEntries.add(Entry(index.toFloat(), wakeTime.time.toFloat()))
                index++
            }
        }

        val sleepDataSet = LineDataSet(sleepEntries, "Sleep Time").apply {
            color = Color.BLUE
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        val wakeDataSet = LineDataSet(wakeEntries, "Wake Time").apply {
            color = Color.GREEN
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        sleepDataSet.color = Color.BLUE
        wakeDataSet.color = Color.GREEN

        val lineData = LineData(sleepDataSet, wakeDataSet).apply {
            setValueTextColor(Color.WHITE)
            setValueTextSize(12f)
        }
        chart.data = lineData
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.axisRight.isEnabled = false

        chart.apply {
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.DKGRAY
                textSize = 12f
                setDrawAxisLine(true)
            }
            axisLeft.apply {
                setDrawGridLines(false)
                textColor = Color.LTGRAY
                textSize = 12f
                setDrawAxisLine(true)
            }

            axisRight.isEnabled = false

            legend.apply {
                textColor = Color.DKGRAY
                textSize = 12f

            }
        }

        chart.invalidate()
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
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    Toast.makeText(requireContext(), "Sleep: $sleepTime, Wake: $wakeTime", Toast.LENGTH_LONG).show()
                    databaseHelper.saveSleepData(date = currentDate, sleepTime, wakeTime, sleepDuration)
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
