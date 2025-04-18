package com.example.movietracker.itemList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movietracker.R

class ItemAdapter(
  private var items: List<Item>,
  private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView = itemView.findViewById(R.id.item_title)
    val subTitle: TextView = itemView.findViewById(R.id.item_subtitle)
    val image: ImageView = itemView.findViewById(R.id.item_image)

    fun bind(item: Item) {
      title.text = item.title
      subTitle.text = item.subTitle
      Glide.with(itemView.context).load(item.imageUrl).into(image)
      itemView.setOnClickListener { onItemClick(item) }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_list, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(items[position])
  }

  override fun getItemCount(): Int = items.size

  fun updateItems(newItems: List<Item>) {
    val diffCallback = ItemDiffCallback(items, newItems)
    val diffResult = DiffUtil.calculateDiff(diffCallback)

    items = newItems
    diffResult.dispatchUpdatesTo(this)
  }
}

class ItemDiffCallback(
  private val oldItems: List<Item>,
  private val newItems: List<Item>
) : DiffUtil.Callback() {
  override fun getOldListSize() = oldItems.size
  override fun getNewListSize() = newItems.size

  override fun areItemsTheSame(oldPos: Int, newPos: Int) =
    oldItems[oldPos].tmbdId == newItems[newPos].tmbdId

  override fun areContentsTheSame(oldPos: Int, newPos: Int) =
    oldItems[oldPos] == newItems[newPos]
}