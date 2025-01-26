package com.example.fitnessappnea

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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

        val nutritionText: EditText = view.findViewById(R.id.nutritionInput)
        val submitButton: Button = view.findViewById(R.id.submitNutritionButton)

        databaseHelper = DatabaseHelper(requireContext(), null)

        submitButton.setOnClickListener {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val inputQuery = nutritionText.text.toString()

            // Make the network request
            requestNutritionData(inputQuery) { response ->
                response?.let {
                    val parseResult = parseResponse(it)

                    // Save the data into the database
                    parseResult.foods.forEach { food ->
                        databaseHelper.saveNutrition(
                            currentDate,
                            food.nf_protein,
                            food.nf_total_carbohydrate,
                            food.nf_total_fat,
                            food.nf_dietary_fiber,
                            food.nf_calories
                        )
                    }

                    println("Nutrition data saved successfully!")
                } ?: run {
                    println("Failed to retrieve nutrition data.")
                }
            }
            refreshNutritionProgressBars(view)
        }


        refreshNutritionProgressBars(view)

        return view
    }

    private fun requestNutritionData(query: String, callback: (String?) -> Unit) {
        val client = OkHttpClient()
        val url = "https://trackapi.nutritionix.com/v2/natural/nutrients"

        val jsonBody = """
        {
            "query": "$query"
        }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("x-app-key", "37f974febc324eb24a49f3a748098660")
            .addHeader("x-app-id", "6ca8f34c")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    println("Response: $responseBody")
                    callback(responseBody)
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
        val proteinProgressBar = view.findViewById<LinearProgressIndicator>(R.id.proteinProgress)
        val carbsProgressBar = view.findViewById<LinearProgressIndicator>(R.id.carbsProgress)
        val fatsProgressBar = view.findViewById<LinearProgressIndicator>(R.id.fatsProgress)
        val fibreProgressBar = view.findViewById<LinearProgressIndicator>(R.id.fibreProgress)

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val nutritionData = databaseHelper.getNutritionData(currentDate)

        if (nutritionData != null) {
            proteinProgressBar?.progress = (nutritionData.protein.toInt() / 60)
            carbsProgressBar?.progress = (nutritionData.carbohydrates.toInt() / 300)
            fatsProgressBar?.progress = (nutritionData.fats.toInt() / 65)
            fibreProgressBar?.progress = (nutritionData.fibre.toInt() / 30)
        }
    }
}
