package com.example.movietracker.fragment.inspectTvShow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.movietracker.databinding.FragmentTvShowDetailsBinding

class TvShowDetailsFragment : Fragment() {
    private lateinit var binding: FragmentTvShowDetailsBinding
    private val viewModel: TvShowViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTvShowDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.tvShow.observe(viewLifecycleOwner) { tvShow ->
            binding.tvShow = tvShow
        }
    }
}