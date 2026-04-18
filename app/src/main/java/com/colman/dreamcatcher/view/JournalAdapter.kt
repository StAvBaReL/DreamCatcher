package com.colman.dreamcatcher.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.colman.dreamcatcher.databinding.JournalPostRowBinding
import com.colman.dreamcatcher.model.DreamPost

class JournalAdapter : RecyclerView.Adapter<JournalViewHolder>() {

    var posts: MutableList<DreamPost> = mutableListOf()
    var onEditClick: ((DreamPost) -> Unit)? = null
    var onDeleteClick: ((DreamPost) -> Unit)? = null

    override fun getItemCount(): Int = posts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val binding =
            JournalPostRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JournalViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        holder.bind(posts[position])
    }
}
