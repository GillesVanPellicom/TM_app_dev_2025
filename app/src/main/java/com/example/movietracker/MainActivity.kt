package com.example.movietracker

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.movietracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    internal lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the NavController with the BottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Handle top padding to take system bar into account
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBarsInsets.top,
            )
            insets
        }

        // Set up the bottom navigation with the NavController
        NavigationUI.setupWithNavController(binding.bottomNav, navController)
    }

    fun showLoadingSpinner() {
        binding.progressBarContainer.visibility = View.VISIBLE
    }

    fun hideLoadingSpinner() {
        binding.progressBarContainer.visibility = View.GONE
    }

    fun showReloadButton() {
        binding.reloadButton.visibility = View.VISIBLE
    }

    fun hideReloadButton() {
        binding.reloadButton.visibility = View.GONE
    }

    fun setupRetryButton(retryButton: View, reloadFunction: () -> Unit) {
        retryButton.setOnClickListener {
            showLoadingSpinner() // Show the loading spinner
            retryButton.visibility = View.GONE // Hide the retry button
            reloadFunction() // Call the reload function
        }
    }
}