package com.colman.dreamcatcher.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "posts")
data class DreamPost(
    @PrimaryKey val postId: String = "",
    val authorUid: String = "",
    val authorNickname: String = "",
    val authorProfilePicUrl: String? = null,
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val createdAt: Long = 0L,
    var lastUpdated: Long = 0L,
    var likes: List<String> = emptyList(),
    var isDeleted: Boolean = false
) {
    companion object {
        const val POST_ID_KEY = "postId"
        const val AUTHOR_UID_KEY = "authorUid"
        const val AUTHOR_NICKNAME_KEY = "authorNickname"
        const val AUTHOR_PROFILE_PIC_URL_KEY = "authorProfilePicUrl"
        const val TITLE_KEY = "title"
        const val DESCRIPTION_KEY = "description"
        const val IMAGE_URL_KEY = "imageUrl"
        const val CREATED_AT_KEY = "createdAt"
        const val LAST_UPDATED_KEY = "lastUpdated"
        const val LIKES_KEY = "likes"
        const val IS_DELETED_KEY = "isDeleted"

        fun fromJson(json: Map<String, Any?>): DreamPost {
            return DreamPost(
                postId = json[POST_ID_KEY] as? String ?: "",
                authorUid = json[AUTHOR_UID_KEY] as? String ?: "",
                authorNickname = json[AUTHOR_NICKNAME_KEY] as? String ?: "",
                authorProfilePicUrl = json[AUTHOR_PROFILE_PIC_URL_KEY] as? String,
                title = json[TITLE_KEY] as? String ?: "",
                description = json[DESCRIPTION_KEY] as? String ?: "",
                imageUrl = json[IMAGE_URL_KEY] as? String ?: "",
                createdAt = json[CREATED_AT_KEY] as? Long ?: 0L,
                lastUpdated = json[LAST_UPDATED_KEY] as? Long ?: 0L,
                likes = (json[LIKES_KEY] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                isDeleted = json[IS_DELETED_KEY] as? Boolean ?: false
            )
        }
    }

    val toJson: Map<String, Any?>
        get() = hashMapOf(
            POST_ID_KEY to postId,
            AUTHOR_UID_KEY to authorUid,
            AUTHOR_NICKNAME_KEY to authorNickname,
            AUTHOR_PROFILE_PIC_URL_KEY to authorProfilePicUrl,
            TITLE_KEY to title,
            DESCRIPTION_KEY to description,
            IMAGE_URL_KEY to imageUrl,
            CREATED_AT_KEY to createdAt,
            LAST_UPDATED_KEY to lastUpdated,
            LIKES_KEY to likes,
            IS_DELETED_KEY to isDeleted
        )
}
