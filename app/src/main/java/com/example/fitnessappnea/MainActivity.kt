package com.example.fitnessappnea

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fitnessappnea.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Binding object instance with access to the views in the activity_main.xml layout
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        replaceFragment(Workout())
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Setting up the navigation menu to show specific fragments for each button
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.workout -> replaceFragment(Workout())
                R.id.nutrition -> replaceFragment(Nutrition())
                R.id.sleep -> replaceFragment(Sleep())

                else -> {}
            }
            true
        }
    }

    // Replace the currently shown fragment with the selected fragment
    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        // Commit changes of fragmentManager to show to the user
        fragmentTransaction.commit()
    }



}