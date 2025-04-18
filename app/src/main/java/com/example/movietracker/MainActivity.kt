package com.example.movietracker

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.Fragment

import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.room.Room
import com.example.movietracker.database.AppDatabase
import com.example.movietracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    internal lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app-database"
        ).build()

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

        // Add a destination change listener to update the active state
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_home -> binding.bottomNav.menu.findItem(R.id.nav_home).isChecked = true
                R.id.nav_films_and_series -> binding.bottomNav.menu.findItem(R.id.nav_films_and_series).isChecked = true
                R.id.nav_liked -> binding.bottomNav.menu.findItem(R.id.nav_liked).isChecked = true
                R.id.nav_inspect_movie, R.id.nav_inspect_tv_show -> {
                    // Keep the Films & Series button active for these destinations
                    binding.bottomNav.menu.findItem(R.id.nav_films_and_series).isChecked = true
                }
            }
        }
    }

    fun showLoadingSpinner(hideBackground: Boolean = false) {
        binding.progressBarContainer.visibility = View.VISIBLE
        if (hideBackground) binding.navHostFragment.visibility = View.GONE

    }

    fun hideLoadingSpinner() {
        binding.progressBarContainer.visibility = View.GONE
        binding.navHostFragment.visibility = View.VISIBLE
    }

    fun showReloadButton(hideBackground: Boolean = false) {
        binding.reloadButtonContainer.visibility = View.VISIBLE
        if (hideBackground) binding.navHostFragment.visibility = View.GONE
    }

    fun hideReloadButton() {
        binding.reloadButtonContainer.visibility = View.GONE
        binding.navHostFragment.visibility = View.VISIBLE
    }

    fun setupRetryButton(reloadButton: View, reloadFunction: () -> Unit) {
        reloadButton.setOnClickListener {
            hideReloadButton()
            showLoadingSpinner()
            reloadFunction()
        }
    }

    fun showErrorDialog() {
        val dialog = Dialog(this, android.R.style.Theme_Material_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)

        // Set full-screen layout parameters
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Inflate a layout for Material 3 dialog content
        val dialogView = LayoutInflater.from(this).inflate(
            R.layout.dialog_fullscreen_error,
            dialog.window?.decorView?.rootView as? ViewGroup,
            false
        )
        dialog.setContentView(dialogView)

        // Configure views
        dialogView.findViewById<TextView>(R.id.dialog_title).text = getString(R.string.ed_title)
        dialogView.findViewById<TextView>(R.id.dialog_message).text = getString(R.string.ed_text)
        dialogView.findViewById<ImageView>(R.id.dialog_icon)
            .setImageResource(R.drawable.ic_error)
        dialogView.findViewById<Button>(R.id.dialog_button_ok).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}