package com.example.fitnessappnea

import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.fitnessappnea.database.CompletedWorkout
import com.example.fitnessappnea.database.DatabaseHelper
import com.example.fitnessappnea.database.SleepData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.data.*
import okhttp3.internal.wait
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class TimeValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        var totalMinutes = value.toInt()

        // Subtract 4 hours
        totalMinutes -= 240

        // Ensure proper wrapping from negative values to the previous day
        if (totalMinutes < 0) {
            totalMinutes += 1440
        }

        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60

        return String.format("%02d:%02d", hours, minutes) // Format as HH:mm
    }
}


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

        val chart: LineChart = view.findViewById(R.id.sleepChart)
        val weekData = databaseHelper.get7DaySleepData()
        displayChart(chart, weekData, view)
        addSleepHistory(view)

//        if (data == null) {
            showSleepTimeDialog(view)
//        }

        return view
    }

    private fun addSleepHistory(view: View) {
        val sleepHistoryContainer: LinearLayout = view.findViewById(R.id.sleepHistoryContainer)

        val sleepHistoryList = databaseHelper.getAllSleepData()
        val sortedSleepHistoryList = mergeSortByDate(sleepHistoryList)

        if (sortedSleepHistoryList.isNotEmpty()) {
            val titleTextView = TextView(requireContext()).apply {
                text = "Sleep History"
                textSize = 18f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
                setPadding(16, 8, 16, 8)
            }
            sleepHistoryContainer.addView(titleTextView)

            for (sleepData in sortedSleepHistoryList) {
                val entryView = createSleepHistoryEntry(sleepData)
                sleepHistoryContainer.addView(entryView)
            }
        }
    }

    private fun createSleepHistoryEntry(sleepData: SleepData): LinearLayout {
        val context = requireContext()

        val entryLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            setBackgroundResource(R.drawable.rounded_background)
        }

        val dateTextView = TextView(context).apply {
            text = sleepData.date?.let { formatDate(it) }
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }

        val sleepTimeTextView = TextView(context).apply {
            text = "- ${sleepData.sleepTime} Sleep"
            textSize = 14f
            setTextColor(Color.LTGRAY)
        }

        val wakeTimeTextView = TextView(context).apply {
            text = "- ${sleepData.wakeTime} Wake"
            textSize = 14f
            setTextColor(Color.LTGRAY)
        }

        val durationTextView = TextView(context).apply {
            text = "- ${formatMinutesToHHmm(sleepData.sleepDuration.toInt())} Total Sleep"
            textSize = 14f
            setTextColor(Color.LTGRAY)
        }

        entryLayout.addView(dateTextView)
        entryLayout.addView(sleepTimeTextView)
        entryLayout.addView(wakeTimeTextView)
        entryLayout.addView(durationTextView)

        return entryLayout
    }

    private fun displayChart(chart: LineChart, data: List<SleepData>, view: View) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentData = databaseHelper.getSleepData(currentDate)

        if (currentData != null) {
            updateSleepData(currentData.lightDuration, currentData.SWSDuration, currentData.REMDuration, view)
        }


        val sleepEntries = mutableListOf<Entry>()
        val wakeEntries = mutableListOf<Entry>()
        val adjustedSleepTimes = mutableListOf<Double>()
        val adjustedWakeTimes = mutableListOf<Double>()

        // Normalise and store adjusted sleep and wake times for plotting
        for ((index, item) in data.withIndex()) {
            val sleepParts = item.sleepTime.split(":").map { it.toInt() }
            val wakeParts = item.wakeTime.split(":").map { it.toInt() }
            val sleepMinutes = sleepParts[0] * 60 + sleepParts[1] // Convert HH:mm to minutes
            val wakeMinutes = wakeParts[0] * 60 + wakeParts[1]

            // Adjust times to fit 20:00 (0) - 10:00 (840 minutes) scale
            val adjustedSleep = if (sleepMinutes >= 1200) sleepMinutes - 1200 else sleepMinutes + 240
            val adjustedWake = if (wakeMinutes >= 1200) wakeMinutes - 1200 else wakeMinutes + 240

            adjustedSleepTimes.add(adjustedSleep.toDouble())
            adjustedWakeTimes.add(adjustedWake.toDouble())
            sleepEntries.add(Entry(index.toFloat(), adjustedSleep.toFloat()))
            wakeEntries.add(Entry(index.toFloat(), adjustedWake.toFloat()))
        }

        // Compute regression on normalised sleep/wake times
        val (sleepSlope, sleepIntercept) = linearRegression((0 until data.size).map { it.toDouble() }, adjustedSleepTimes)
        val (wakeSlope, wakeIntercept) = linearRegression((0 until data.size).map { it.toDouble() }, adjustedWakeTimes)

        val linearSleepEntries = mutableListOf<Entry>()
        val linearWakeEntries = mutableListOf<Entry>()

        // Generate regression lines that align with the transformed graph scale
        for (i in 0 until data.size) {
            val x = i.toFloat()
            linearSleepEntries.add(Entry(x, (sleepSlope * x + sleepIntercept).toFloat()))
            linearWakeEntries.add(Entry(x, (wakeSlope * x + wakeIntercept).toFloat()))
        }

        val linearSleepDataSet = LineDataSet(linearSleepEntries, "Sleep Regression").apply {
            color = Color.parseColor("#FF5733")
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curves
            lineWidth = 1.5f // Set line thickness
            setDrawCircles(false) // Show data points
            setDrawValues(false) // Show values on the graph
            valueFormatter = TimeValueFormatter()
        }

        val linearWakeDataSet = LineDataSet(linearWakeEntries, "Wake Regression").apply {
            color = Color.parseColor("#33FF57")
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curves
            lineWidth = 1.5f // Set line thickness
            setDrawCircles(false) // Show data points
            setDrawValues(false) // Show values on the graph
            valueFormatter = TimeValueFormatter()
        }

        val sleepDataSet = LineDataSet(sleepEntries, "Sleep Time").apply {
            color = Color.parseColor("#4B0082")
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curves
            lineWidth = 2.5f // Set line thickness
            setDrawCircles(true) // Show data points
            setCircleColor(Color.parseColor("#4B0082")) // Matching circle color
            setDrawValues(true) // Show values on the graph
            valueFormatter = TimeValueFormatter()
        }

        val wakeDataSet = LineDataSet(wakeEntries, "Wake Time").apply {
            color = Color.parseColor("#FFA500")
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curves
            lineWidth = 2.5f // Set line thickness
            setDrawCircles(true) // Show data points
            setCircleColor(Color.parseColor("#FFA500")) // Matching circle color
            setDrawValues(true) // Show values on the graph
            valueFormatter = TimeValueFormatter()
        }


        val lineData = LineData(sleepDataSet, wakeDataSet, linearSleepDataSet, linearWakeDataSet).apply {
            setValueTextColor(Color.WHITE)
            setValueTextSize(12f)
        }

        chart.data = lineData

        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false

        // Convert values from the integer from 20:00 to hour and minute values for the axis
        chart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val adjustedValue = (value.toInt() + 1200) % 1440 // Convert back to HH:mm
                val hours = adjustedValue / 60
                val minutes = adjustedValue % 60
                return String.format("%02d:%02d", hours, minutes)
            }
        }

        // Format y axis
        chart.axisLeft.apply {
            granularity = 60f  // 1-hour intervals for y axis
            setDrawGridLines(true)
            textColor = Color.WHITE
            textSize = 12f
            isInverted = true
        }

        // Format x axis
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            textColor = Color.LTGRAY
            textSize = 12f
        }

        // Format legend
        chart.legend.apply {
            textColor = Color.WHITE
            textSize = 13f
            formSize = 13f
            isWordWrapEnabled = true // Issue with legend going off screen so wrap the text
        }

        chart.invalidate() // Refresh chart
    }

    private fun updateSleepData(lightSleep: Int, swsSleep: Int, remSleep: Int, View: View) {
        val lightSleepTextView: TextView = View.findViewById(R.id.lightSleepTextView)
        val swsSleepTextView: TextView = View.findViewById(R.id.swsSleepTextView)
        val remSleepTextView: TextView = View.findViewById(R.id.remSleepTextView)

        lightSleepTextView.text = "- ${formatMinutesToHHmm(lightSleep)} Light"
        swsSleepTextView.text = "- ${formatMinutesToHHmm(swsSleep)} SWS (Deep)"
        remSleepTextView.text = "- ${formatMinutesToHHmm(remSleep)} REM"
    }

    private fun showSleepTimeDialog(view: View) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter Sleep & Wake Time")

        builder.setMessage("Please select your sleep and wake time.")

        builder.setPositiveButton("Set Time") { _, _ ->
            showTimePicker("Select Sleep Time") { sleepHour, sleepMinute ->
                showTimePicker("Select Wake Time") { wakeHour, wakeMinute ->
                    // Verify wake time not before sleep time
                    if (wakeHour < sleepHour || (wakeHour == sleepHour && wakeMinute < sleepMinute)) {
                        Toast.makeText(requireContext(), "Wake time must be after sleep time", Toast.LENGTH_LONG).show()
                        showSleepTimeDialog(view)
                        return@showTimePicker
                    }


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
                    databaseHelper.saveSleepData(date = currentDate, sleepTime, wakeTime, sleepDuration.toDouble())

                    // Refresh chart
                    val chart: LineChart = view.findViewById(R.id.sleepChart)
                    val weekData = databaseHelper.get7DaySleepData()
                    displayChart(chart, weekData, view)
                    addSleepHistory(view)
                }
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }


    private fun linearRegression(x: List<Double>, y: List<Double>): Pair<Double, Double> {
        // Need to find sum of x, sum of y, sum of x^2 and sum of xy in order to apply linear regression formula
        val n = x.size
        val sumX = x.sum()
        val sumY = y.sum()
        val sumXX = x.map { it * it }.sum()
        val sumXY = x.zip(y).map { it.first * it.second }.sum()

        // Find equation
        val slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX)
        val intercept = (sumY - slope * sumX) / n

        return Pair(slope, intercept) // Return slope and intercept
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

    private fun formatDate(inputDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Get the date using old format
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Format into new format
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date ?: inputDate)
        } catch (e: Exception) {
            inputDate // Return original if parsing fails
        }
    }

    private fun formatMinutesToHHmm(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return String.format("%02d:%02d", hours, mins)
    }


    // Same as Workout.kt but modified to work with the SleepData data class
    private fun mergeSortByDate(sleepDataList: List<SleepData>): List<SleepData> {
        // 1 or 0 elements and the list doesnt need to be sorted
        if (sleepDataList.size <= 1) {
            return sleepDataList
        }

        val mid = sleepDataList.size / 2
        val leftSorted = mergeSortByDate(sleepDataList.subList(0, mid)) // Sort the left half using function recursion
        val rightSorted = mergeSortByDate(sleepDataList.subList(mid, sleepDataList.size)) // Sort the right half

        return merge(leftSorted, rightSorted) // Merge the sorted halves
    }

    private fun merge(left: List<SleepData>, right: List<SleepData>): List<SleepData> {
        var leftIndex = 0
        var rightIndex = 0
        val mergedList = mutableListOf<SleepData>()

        // Merge both sorted lists in descending order based on the 'date' property.
        while (leftIndex < left.size && rightIndex < right.size) {
            // Handle nullable dates by converting null to an empty string.
            val leftDate = left[leftIndex].date ?: ""
            val rightDate = right[rightIndex].date ?: ""

            // Compare dates in descending order (later dates come first).
            if (leftDate >= rightDate) {
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
