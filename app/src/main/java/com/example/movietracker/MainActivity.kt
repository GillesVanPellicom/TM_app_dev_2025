package com.example.movietracker

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.ui.setupWithNavController
import com.example.movietracker.database.AppDatabase
import com.example.movietracker.databinding.ActivityMainBinding
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat


class MainActivity : AppCompatActivity() {

  internal lateinit var binding: ActivityMainBinding
  private lateinit var navController: NavController
  private var isBackDisabled: Boolean = false // Flag to disable back button

  private var isErrorDialogShown = false // Flag to prevent multiple error dialogs

  private val NOTIFICATION_PERMISSION_REQUEST_CODE = 101

  companion object {
    lateinit var database: AppDatabase

    // Source tab tracking - maps a detail screen instance to its source tab
    val detailScreenSources = mutableMapOf<Int, Int>()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Initialize Room database
    database = androidx.room.Room.databaseBuilder(
      applicationContext,
      AppDatabase::class.java,
      "movie-tracker-database"
    ).fallbackToDestructiveMigration()
      .build()

    // Handle top padding to take system bar into account
    ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { view, insets ->
      val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      view.updatePadding(top = systemBarsInsets.top)
      insets
    }

    // Register OnBackPressedCallback
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        if (!isBackDisabled) {
          isEnabled = false // Temporarily disable the callback
          onBackPressedDispatcher.onBackPressed() // Trigger default behavior
          isEnabled = true // Re-enable the callback
        }
      }
    })

    // Set system bar colors for cross compatibility
    setupSystemBars()


    // Get NavController properly from the NavHostFragment
    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        as androidx.navigation.fragment.NavHostFragment
    navController = navHostFragment.navController

    // Setup bottom navigation with NavController
    binding.bottomNav.setupWithNavController(navController)

    // Handle tab reselection to pop back to tab's root page
    binding.bottomNav.setOnItemReselectedListener { menuItem ->
      when (menuItem.itemId) {
        R.id.nav_home -> navController.popBackStack(R.id.nav_home, false)
        R.id.nav_films_and_series -> navController.popBackStack(R.id.nav_films_and_series, false)
        R.id.nav_liked -> navController.popBackStack(R.id.nav_liked, false)
      }
    }

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

    requestNotificationPermission()
  }

  private fun requestNotificationPermission() {
    // Request permissions for notifications
    // No API check required since android target is 13+
    if (ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
        NOTIFICATION_PERMISSION_REQUEST_CODE
      )
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Log.d("MainActivity", "POST_NOTIFICATIONS permission granted")
      } else {
        Log.w("MainActivity", "POST_NOTIFICATIONS permission denied")
      }
    }
  }

  fun showLoadingSpinner(hideBackground: Boolean = false) {
    binding.progressBarContainer.visibility = View.VISIBLE
    isBackDisabled = true
    if (hideBackground) binding.navHostFragment.visibility = View.GONE

  }

  fun hideLoadingSpinner() {
    binding.progressBarContainer.visibility = View.GONE
    isBackDisabled = false
    binding.navHostFragment.visibility = View.VISIBLE
  }

  fun showReloadButton(hideBackground: Boolean = false) {
    binding.reloadButtonContainer.visibility = View.VISIBLE
    isBackDisabled = true
    if (hideBackground) binding.navHostFragment.visibility = View.GONE
  }

  fun hideReloadButton() {
    binding.reloadButtonContainer.visibility = View.GONE
    isBackDisabled = false
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
    // Prevent multiple dialogs
    if (isErrorDialogShown) return
    isErrorDialogShown = true

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
      R.layout.dialog_fullscreen,
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
      isErrorDialogShown = false
      dialog.dismiss()
    }

    dialog.show()
  }

  private fun setupSystemBars() {
    // Make the app draw behind system bars (edge-to-edge)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    // Get WindowInsetsController
    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

    // Determine dark mode status
    val nightModeFlags = resources.configuration.uiMode and
        android.content.res.Configuration.UI_MODE_NIGHT_MASK
    val isNightMode = nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES

    // Set light/dark appearance based on theme
    windowInsetsController.isAppearanceLightStatusBars = !isNightMode
    windowInsetsController.isAppearanceLightNavigationBars = !isNightMode

    // Optional: For a more integrated look, add this line
    windowInsetsController.systemBarsBehavior =
      WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
  }
}
