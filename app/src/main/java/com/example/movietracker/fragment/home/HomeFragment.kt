package com.example.movietracker.fragment.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.movietracker.R
import com.example.movietracker.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
    }
}