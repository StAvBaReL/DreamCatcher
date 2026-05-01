package com.colman.dreamcatcher.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colman.dreamcatcher.model.DreamPost

@Dao
interface DreamPostDao {
    @Query("SELECT * FROM posts WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllPostsPaged(): androidx.paging.PagingSource<Int, DreamPost>

    @Query("SELECT * FROM posts WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllPosts(): LiveData<List<DreamPost>>

    @Query("SELECT * FROM posts WHERE authorUid = :uid AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getPostsByUserPaged(uid: String): androidx.paging.PagingSource<Int, DreamPost>

    @Query("SELECT * FROM posts WHERE authorUid = :uid AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getPostsByUser(uid: String): LiveData<List<DreamPost>>

    @Query("SELECT postId FROM posts")
    fun getAllPostIds(): List<String>

    @Query("SELECT COUNT(*) FROM posts WHERE isDeleted = 0")
    fun countActivePosts(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPosts(post: DreamPost)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPostsList(posts: List<DreamPost>)

    @Query("DELETE FROM posts WHERE postId = :postId")
    fun deletePostById(postId: String)
}
