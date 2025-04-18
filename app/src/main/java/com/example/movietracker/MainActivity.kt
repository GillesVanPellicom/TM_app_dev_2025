package com.example.movietracker

import android.app.Dialog
import android.os.Bundle
import android.util.Log
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
import kotlin.text.get
import kotlin.text.set
import kotlin.times

class MainActivity : AppCompatActivity() {

  internal lateinit var binding: ActivityMainBinding
  private lateinit var navController: NavController

  companion object {
    lateinit var database: AppDatabase

    // Source tab tracking - maps a detail screen instance to its source tab
    val detailScreenSources = mutableMapOf<Int, Int>()
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
      view.updatePadding(top = systemBarsInsets.top)
      insets
    }

    NavigationUI.setupWithNavController(binding.bottomNav, navController)

    // Destination change listener to handle navigation
    navController.addOnDestinationChangedListener { _, destination, arguments ->
      try {
        when (destination.id) {
          R.id.nav_home, R.id.nav_films_and_series, R.id.nav_liked -> {
            // For main tabs, safely select them directly
            val menuItem = binding.bottomNav.menu.findItem(destination.id)
            menuItem?.isChecked = true
          }

          R.id.nav_inspect_movie, R.id.nav_inspect_tv_show -> {
            // Get source tab and item ID
            val sourceTabId = arguments?.getInt("sourceTabId", -1) ?: -1
            val itemId = arguments?.getInt("id", -1) ?: -1

            // Create a unique key for this detail screen instance
            val screenKey = destination.id * 100000 + itemId

            if (sourceTabId != -1) {
              // Store the source tab for this specific detail screen
              detailScreenSources[screenKey] = sourceTabId
            }

            // Get the tab to highlight (source tab or fallback to films & series)
            val tabToHighlight = detailScreenSources[screenKey] ?: R.id.nav_films_and_series

            // Check if the menu item exists before setting it as checked
            val menuItem = binding.bottomNav.menu.findItem(tabToHighlight)
            menuItem?.isChecked = true
          }
        }
      } catch (e: Exception) {
        Log.e("MainActivity", "Error handling destination change: ${e.message}")
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