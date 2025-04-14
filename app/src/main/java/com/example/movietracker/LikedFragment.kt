package com.example.movietracker

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movietracker.databinding.FragmentLikedBinding
import com.example.movietracker.itemList.Item
import com.example.movietracker.itemList.ItemAdapter

class LikedFragment : Fragment(R.layout.fragment_liked) {
    private lateinit var binding: FragmentLikedBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLikedBinding.bind(view)

//        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
//            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.updatePadding(top = systemBarsInsets.top)
//            insets
//        }

        binding.recyclerView.clipToPadding = false


        // Sample data for RecyclerView
        val items = listOf(
            Item(R.mipmap.ic_img_1, "Title 1", "Subtitle 1"),
            Item(R.mipmap.ic_img_1, "Title 2","Subtitle 2"),
            Item(R.mipmap.ic_img_1, "Title 3","Subtitle 3"),
            Item(R.mipmap.ic_img_1, "Title 4","Subtitle 4"),
            Item(R.mipmap.ic_img_1, "Title 5","Subtitle 5"),
            Item(R.mipmap.ic_img_1, "Title 6","Subtitle 6"),
            Item(R.mipmap.ic_img_1, "Title 7","Subtitle 7"),
            Item(R.mipmap.ic_img_1, "Title 8","Subtitle 8"),
            Item(R.mipmap.ic_img_1, "Title 9","Subtitle 9"),
            Item(R.mipmap.ic_img_1, "Title 10","Subtitle 10"),
            Item(R.mipmap.ic_img_1, "Title 11","Subtitle 11"),
            Item(R.mipmap.ic_img_1, "Title 12","Subtitle 12"),
            Item(R.mipmap.ic_img_1, "Title 13","Subtitle 13"),
            Item(R.mipmap.ic_img_1, "Title 14","Subtitle 14"),
            Item(R.mipmap.ic_img_1, "Title 15","Subtitle 15")
        )

        // Setup RecyclerView with adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = ItemAdapter(items)
    }
}