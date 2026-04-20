package com.colman.dreamcatcher.view

import android.text.format.DateUtils
import androidx.recyclerview.widget.RecyclerView
import com.colman.dreamcatcher.databinding.FeedPostRowBinding
import com.colman.dreamcatcher.model.DreamPost
import com.squareup.picasso.Picasso

class FeedViewHolder(
    private val binding: FeedPostRowBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: DreamPost) {
        Picasso.get()
            .load(post.imageUrl)
            .into(binding.ivDreamImage)

        val initials = post.authorNickname
            .trim()
            .take(2)
            .uppercase()
        binding.tvAuthorInitials.text = initials
        binding.tvAuthorName.text = post.authorNickname

        binding.tvTimestamp.text = DateUtils.getRelativeTimeSpanString(
            post.createdAt,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )

        binding.tvTitle.text = post.title
        binding.tvDescription.text = post.description
    }
}
