package com.colman.dreamcatcher.view

import android.graphics.PorterDuff
import android.text.format.DateUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.colman.dreamcatcher.R
import com.colman.dreamcatcher.databinding.FeedPostRowBinding
import com.colman.dreamcatcher.model.DreamCatcherModel
import com.colman.dreamcatcher.model.DreamPost
import com.colman.dreamcatcher.utils.CircleTransform

class FeedViewHolder(
    private val binding: FeedPostRowBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        post: DreamPost,
        currentUserId: String,
        onLikeClick: ((DreamPost) -> Unit)?,
        onEditClick: ((DreamPost) -> Unit)?,
        onDeleteClick: ((DreamPost) -> Unit)?
    ) {
        Picasso.get()
            .load(post.imageUrl)
            .into(binding.ivDreamImage)

        val profileUrl = post.authorProfilePicUrl
            ?: if (post.authorUid == currentUserId) {
                DreamCatcherModel.getCurrentUser()?.photoUrl?.toString()
            } else {
                null
            }
        if (!profileUrl.isNullOrEmpty()) {
            binding.ivAuthorAvatar.visibility = View.VISIBLE
            Picasso.get()
                .load(profileUrl)
                .transform(CircleTransform())
                .into(binding.ivAuthorAvatar)
        } else {
            binding.ivAuthorAvatar.visibility = View.GONE
        }

        val initials = post.authorNickname.trim().take(2).uppercase()
        binding.tvAuthorInitials.text = initials
        binding.tvAuthorName.text = post.authorNickname

        binding.tvTimestamp.text = DateUtils.getRelativeTimeSpanString(
            post.createdAt,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )

        binding.tvTitle.text = post.title
        binding.tvDescription.text = post.description

        val isLiked = currentUserId.isNotEmpty() && currentUserId in post.likes
        binding.tvLikeCount.text = post.likes.size.toString()
        val likeColor = if (isLiked) {
            ContextCompat.getColor(binding.root.context, R.color.purple_primary)
        } else {
            ContextCompat.getColor(binding.root.context, R.color.text_secondary)
        }
        binding.ivLikeButton.setColorFilter(likeColor, PorterDuff.Mode.SRC_IN)
        binding.ivLikeButton.setOnClickListener { onLikeClick?.invoke(post) }

        if (post.authorUid == currentUserId) {
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnEdit.setOnClickListener { onEditClick?.invoke(post) }
            binding.btnDelete.setOnClickListener { onDeleteClick?.invoke(post) }
        } else {
            binding.btnEdit.visibility = View.GONE
            binding.btnDelete.visibility = View.GONE
        }
    }
}
