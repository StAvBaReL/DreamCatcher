package com.colman.dreamcatcher.view

import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.colman.dreamcatcher.databinding.JournalPostRowBinding
import com.colman.dreamcatcher.model.DreamPost
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JournalViewHolder(
    private val binding: JournalPostRowBinding,
    private val onEditClick: ((DreamPost) -> Unit)?,
    private val onDeleteClick: ((DreamPost) -> Unit)?
) : RecyclerView.ViewHolder(binding.root) {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    fun bind(post: DreamPost) {
        binding.tvTitle.text = post.title
        binding.tvDescription.text = post.description
        binding.tvTimestamp.text = dateFormat.format(Date(post.createdAt))

        Picasso.get()
            .load(post.imageUrl)
            .into(binding.ivDreamImage)

        binding.btnEdit.setOnClickListener { onEditClick?.invoke(post) }
        binding.btnDelete.setOnClickListener { onDeleteClick?.invoke(post) }
    }
}
