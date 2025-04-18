package com.example.movietracker

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.movietracker.api.Creator
import com.example.movietracker.api.Genre
import com.example.movietracker.api.Network
import com.example.movietracker.api.ProductionCompany
import com.example.movietracker.api.ProductionCountry
import com.example.movietracker.api.SpokenLanguage
import kotlin.collections.joinToString

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    if (url != null) {
        Glide.with(view.context)
            .load(url)
            .into(view)
    }
}

@BindingAdapter("genresText")
fun setGenresText(view: TextView, genres: List<Genre>?) {
    view.text = genres?.joinToString(", ") { it.name } ?: ""
}

@BindingAdapter("companiesText")
fun setCompaniesText(view: TextView, companies: List<ProductionCompany>?) {
    view.text = companies?.joinToString(", ") { it.name } ?: ""
}

@BindingAdapter("countriesText")
fun setCountriesText(view: TextView, countries: List<ProductionCountry>?) {
    view.text = countries?.joinToString(", ") { it.name } ?: ""
}

@BindingAdapter("languagesText")
fun setLanguagesText(view: TextView, languages: List<SpokenLanguage>?) {
    view.text = languages?.joinToString(", ") { it.englishName } ?: ""
}

@BindingAdapter("runtimeText")
fun setRuntimeText(view: TextView, runtime: Int?) {
    if (runtime != null) {
        val hours = runtime / 60
        val minutes = runtime % 60
        view.text = "${hours}h ${minutes}m"
    } else {
        view.text = ""
    }
}

@BindingAdapter("moneyText")
fun setMoneyText(view: TextView, amount: Int?) {
    if (amount != null && amount > 0) {
        view.text = "$${String.format("%,d", amount)}"
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("taglineText")
fun setTaglineText(view: TextView, tagline: String?) {
    if (!tagline.isNullOrEmpty()) {
        view.text = "\"$tagline\""
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("createdByText")
fun setCreatedByText(textView: TextView, createdBy: List<Creator>?) {
    textView.text = createdBy?.joinToString(", ") { it.name } ?: ""
}

@BindingAdapter("networksText")
fun setNetworksText(view: TextView, networks: List<Network>?) {
    view.text = networks?.joinToString(", ") { it.name } ?: ""
}