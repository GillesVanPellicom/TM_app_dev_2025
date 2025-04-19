package com.example.movietracker.fragment.home

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.movietracker.R
import com.example.movietracker.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
  private lateinit var binding: FragmentHomeBinding

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding = FragmentHomeBinding.bind(view)

    // Set up the TopAppBar menu
    binding.topAppBar.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.menu_credits -> {
          showCreditsDialog()
          true
        }
        R.id.menu_project_info -> {
          showProjectDialog()
          true
        }
        else -> false
      }
    }
  }

  private fun showCreditsDialog() {
    val dialog = Dialog(requireContext(), android.R.style.Theme_Material_Dialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)

    // Set full-screen layout parameters.
    dialog.window?.setLayout(
      WindowManager.LayoutParams.MATCH_PARENT,
      WindowManager.LayoutParams.MATCH_PARENT
    )
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    // Inflate dialog content.
    val dialogView = LayoutInflater.from(requireContext()).inflate(
      R.layout.dialog_fullscreen,
      dialog.window?.decorView?.rootView as? ViewGroup,
      false
    )
    dialog.setContentView(dialogView)

    // Configure views.
    dialogView.findViewById<TextView>(R.id.dialog_title).text = getString(R.string.cdiag_title)
    dialogView.findViewById<TextView>(R.id.dialog_message).text = getString(R.string.cdiag_text)
    dialogView.findViewById<ImageView>(R.id.dialog_icon).setImageResource(R.drawable.ic_person)
    dialogView.findViewById<Button>(R.id.dialog_button_ok).setOnClickListener {
      dialog.dismiss()
    }
    dialog.show()
  }

  private fun showProjectDialog() {
    val dialog = Dialog(requireContext(), android.R.style.Theme_Material_Dialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)

    // Set full-screen layout parameters.
    dialog.window?.setLayout(
      WindowManager.LayoutParams.MATCH_PARENT,
      WindowManager.LayoutParams.MATCH_PARENT
    )
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    // Inflate dialog content.
    val dialogView = LayoutInflater.from(requireContext()).inflate(
      R.layout.dialog_fullscreen,
      dialog.window?.decorView?.rootView as? ViewGroup,
      false
    )
    dialog.setContentView(dialogView)

    // Configure views.
    dialogView.findViewById<TextView>(R.id.dialog_title).text = getString(R.string.pdiag_title)
    dialogView.findViewById<TextView>(R.id.dialog_message).text = getString(R.string.pdiag_text)
    dialogView.findViewById<ImageView>(R.id.dialog_icon).setImageResource(R.drawable.ic_info)
    dialogView.findViewById<Button>(R.id.dialog_button_ok).setOnClickListener {
      dialog.dismiss()
    }
    dialog.show()
  }
}