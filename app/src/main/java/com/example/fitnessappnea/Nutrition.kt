package com.example.fitnessappnea

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.fitnessappnea.database.DatabaseHelper
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
            val string = nutritionText.text.toString()
            var data = requestNutritionData(string)
            var parseResult = parseResponse(data)
            println(parseResult)
            parseResult.foods.forEach {
                databaseHelper.saveNutrition(currentDate, it.nf_protein, it.nf_total_carbohydrate, it.nf_total_fat, it.nf_dietary_fiber, it.nf_calories)
            }

        }

        return view
    }


    fun requestNutritionData(string: String): String {
        val client = OkHttpClient()

        val url = "https://trackapi.nutritionix.com/v2/natural/nutrients"

        val jsonBody = """
        {
            "query": "$string"
        }
        """.trimIndent()

        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

        // Build the request with headers and body
        val request = Request.Builder()
            .url(url)
            .addHeader("x-app-key", "37f974febc324eb24a49f3a748098660")
            .addHeader("x-app-id", "6ca8f34c")
            .post(requestBody)
            .build()

        var result: String = ""

        // Execute
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    println("Response: ${response.body?.string()}")

                } else {
                    println("Request failed with code: ${response.code}")
                }
            }
        })
        return result
    }

    fun parseResponse(response: String): FoodResponse {
        val gson = Gson()
        val foodResponse = gson.fromJson(response, FoodResponse::class.java)

        return foodResponse
    }
}