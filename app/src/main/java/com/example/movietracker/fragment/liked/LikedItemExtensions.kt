package com.example.movietracker.fragment.liked

import android.view.View
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.movietracker.MainActivity
import com.example.movietracker.R
import com.example.movietracker.itemList.Item
import com.example.movietracker.notifications.NotificationHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Extension function to handle toggling liked status with undo functionality
 */
fun Fragment.toggleLikeStatus(
  fab: ImageView,
  tmbdId: Int,
  title: String,
  imageUrl: String,
  date: String?,
  isFilm: Boolean,
  rootView: View
) {
  lifecycleScope.launch {
    // Check if item already exists in database
    val items = MainActivity.Companion.database.itemDao().getItemsByType(isFilm)
    val existingItem = items.find { it.tmbdId == tmbdId }

    val message: String
    val newIcon: Int

    if (existingItem != null) {
      // Item exists - remove it
      MainActivity.Companion.database.itemDao().delete(existingItem)
      message = getString(R.string.removed_from_liked, title)
      newIcon = R.drawable.ic_liked
      fab.setImageResource(newIcon)

      showAnchoredSnackbar(rootView, message, existingItem, true, fab, newIcon)
      NotificationHelper.notifyLiked(requireContext(), title, liked = false)
    } else {
      // Item doesn't exist - add it
      val item = Item(
        tmbdId = tmbdId,
        imageUrl = imageUrl,
        title = title,
        subTitle = date?.take(4) ?: "",
        isFilm = isFilm
      )
      MainActivity.Companion.database.itemDao().insert(item)
      message = getString(R.string.added_to_liked, title)
      newIcon = R.drawable.ic_liked_filled
      fab.setImageResource(newIcon)

      showAnchoredSnackbar(rootView, message, item, false, fab, newIcon)
      NotificationHelper.notifyLiked(requireContext(), title, liked = true)
    }
  }
}

private fun Fragment.showAnchoredSnackbar(
  rootView: View,
  message: String,
  item: Item,
  wasRemoved: Boolean,
  fab: ImageView,
  currentIcon: Int
) {
  // Find CoordinatorLayout parent if available
  val parent = findSuitableParent(rootView) ?: rootView

  val snackbar = Snackbar.make(parent, message, Snackbar.LENGTH_LONG)
  snackbar.setAction(R.string.undo) {
    lifecycleScope.launch {
      if (wasRemoved) {
        // Restore item on undo
        MainActivity.Companion.database.itemDao().insert(item)
        fab.setImageResource(R.drawable.ic_liked_filled)
      } else {
        // Remove item on undo
        val itemToDelete = MainActivity.Companion.database.itemDao().getItemsByType(item.isFilm)
          .find { it.tmbdId == item.tmbdId }
        if (itemToDelete != null) {
          MainActivity.Companion.database.itemDao().delete(itemToDelete)
          fab.setImageResource(R.drawable.ic_liked)
        }
      }
    }
  }

  // Prevent the snackbar from moving the FAB
  snackbar.anchorView = fab
  snackbar.show()
}

// Helper function to find a suitable parent for the Snackbar
private fun findSuitableParent(view: View): CoordinatorLayout? {
  var currentView = view
  while (true) {
    if (currentView is CoordinatorLayout) {
      return currentView
    }

    val parent = currentView.parent
    if (parent is View) {
      currentView = parent
    } else {
      return null
    }
  }
}