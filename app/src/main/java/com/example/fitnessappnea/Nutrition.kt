package com.example.fitnessappnea

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.example.fitnessappnea.database.DatabaseHelper
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Define classes for json parsing
data class Food(
    val food_name: String,
    val serving_qty: Int,
    val serving_unit: String,
    val nf_calories: Double,
    val nf_total_fat: Double,
    val nf_total_carbohydrate: Double,
    val nf_protein: Double,
    val nf_dietary_fiber: Double
)

data class FoodResponse(
    val foods: List<Food>
)

class Nutrition : Fragment() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_nutrition, container, false)

        // Get components from XML
        val nutritionText: EditText = view.findViewById(R.id.nutritionInput)
        val submitButton: Button = view.findViewById(R.id.submitNutritionButton)

        databaseHelper = DatabaseHelper(requireContext(), null)

        // Populate the nutrition progress bars
        refreshNutritionProgressBars(view)

        submitButton.setOnClickListener { // set OnClick listener for submit button
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val inputQuery = nutritionText.text.toString() // Get the user inputted food string

            // Make the network request
            requestNutritionData(inputQuery) { response ->
                response?.let {
                    val parseResult = parseResponse(it)

                    // iterate through each food item and save into database
                    parseResult.foods.forEach { food ->
                        databaseHelper.saveNutrition(
                            currentDate,
                            food.nf_protein,
                            food.nf_total_carbohydrate,
                            food.nf_total_fat,
                            food.nf_dietary_fiber,
                            food.nf_calories,
                            food.food_name
                        )
                    }

                    // Refresh the nutrition progress bars
                    requireActivity().runOnUiThread { // Using the UI thread as the UI cannot be accessed fro a background thread (which is what the request is executed on)
                        refreshNutritionProgressBars(view)
                    }

                    println("Nutrition data saved successfully!")
                } ?: run {
                    println("Failed to retrieve nutrition data.")
                }
            }
        }
        return view
    }

    private fun requestNutritionData(query: String, callback: (String?) -> Unit) {
        val client = OkHttpClient() // Use an OkHTTPClient to make the request
        val url = "https://trackapi.nutritionix.com/v2/natural/nutrients"

        // Construct json body to be sent to Nutritionix
        val jsonBody = """{
            "query": "$query"
        }""".trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder() // Build the request
            .url(url)
            .addHeader("x-app-key", "37f974febc324eb24a49f3a748098660")
            .addHeader("x-app-id", "6ca8f34c")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback { // Post the request to Nutritionix
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
                callback(null)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    callback(responseBody) // Pass the response to the callback function
                } else {
                    println("Request failed with code: ${response.code}")
                    callback(null)
                }
            }
        })
    }

    private fun parseResponse(response: String): FoodResponse {
        val gson = Gson()
        return gson.fromJson(response, FoodResponse::class.java)
    }

    private fun refreshNutritionProgressBars(view: View) {
        // Get all progress and label components from XML file
        val proteinProgressBar = view.findViewById<LinearProgressIndicator>(R.id.proteinProgress)
        val carbsProgressBar = view.findViewById<LinearProgressIndicator>(R.id.carbohydratesProgress)
        val fatsProgressBar = view.findViewById<LinearProgressIndicator>(R.id.fatsProgress)
        val fibreProgressBar = view.findViewById<LinearProgressIndicator>(R.id.fibreProgress)
        val proteinText = view.findViewById<TextView>(R.id.proteinLabel)
        val carbsText = view.findViewById<TextView>(R.id.carbohydratesLabel)
        val fatsText = view.findViewById<TextView>(R.id.fatsLabel)
        val fibreText = view.findViewById<TextView>(R.id.fibreLabel)

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val nutritionData = databaseHelper.getNutritionData(currentDate)

        // Update all macronutrient progess bars and labels
        if (nutritionData != null) {
            if (nutritionData.protein.toInt() < 70) {
                proteinProgressBar?.progress = nutritionData.protein.toInt()
                proteinText?.text = "Protein - " + Math.round(nutritionData.protein).toString() + "g"
            } else {
                proteinProgressBar?.progress = 70
                proteinText?.text = "Protein - " + Math.round(nutritionData.protein).toString() + "g"
            }
            if (nutritionData.carbohydrates.toInt() < 300) {
                carbsProgressBar?.progress = nutritionData.carbohydrates.toInt()
                carbsText?.text = "Carbohydrates - " + Math.round(nutritionData.carbohydrates).toString() + "g"
            } else {
                carbsProgressBar?.progress = 300
                carbsText?.text = "Carbohydrates - " + Math.round(nutritionData.carbohydrates).toString() + "g"
            }
            if (nutritionData.fats.toInt() < 70) {
                fatsProgressBar?.progress = nutritionData.fats.toInt()
                fatsText?.text = "Fat - " + Math.round(nutritionData.fats).toString() + "g"
            } else {
                fatsProgressBar?.progress = 70
                fatsText?.text = "Fat - " + Math.round(nutritionData.fats).toString() + "g"
            }
            if (nutritionData.fibre.toInt() < 35) {
                fibreProgressBar?.progress = nutritionData.fibre.toInt()
                fibreText?.text = "Fibre - " + Math.round(nutritionData.fibre).toString() + "g"
            } else {
                fibreProgressBar?.progress = 35
                fibreText?.text = "Fibre - " + Math.round(nutritionData.fibre).toString() + "g"
            }
        }

        val foodItemsContainer = view.findViewById<LinearLayout>(R.id.foodItemsContainer)
        foodItemsContainer.removeAllViews()

        val foodItems = databaseHelper.getFoodList(currentDate)

        // Iterate through each food item and display it into the food items container
        for (item in foodItems) {
            val foodCard = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                setBackgroundResource(R.drawable.rounded_background)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
            }

            var name = item.foodName
            if (name != null) {
                name = name.substring(0, 1).uppercase() + name.substring(1)
            }

            // Create a text views to display food information
            val nameTextView = TextView(requireContext()).apply {
                text = name
                textSize = 16f
                setTextColor(Color.WHITE)
            }

            val detailsTextView = TextView(requireContext()).apply {
                text = "Protein: ${item.protein}g, Carbs: ${item.carbohydrates}g, Fats: ${item.fats}g"
                textSize = 14f
                setTextColor(Color.parseColor("#b3b3b3"))
            }

            // Add everything to their respective view
            foodCard.addView(nameTextView)
            foodCard.addView(detailsTextView)
            foodItemsContainer.addView(foodCard)
        }
    }
}
