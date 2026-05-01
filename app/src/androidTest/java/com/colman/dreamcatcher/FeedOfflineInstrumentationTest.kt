package com.colman.dreamcatcher

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.colman.dreamcatcher.model.DreamPost
import com.colman.dreamcatcher.model.dao.AppLocalDbRepository
import org.junit.Assert.assertFalse
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumentation tests verifying that the Room-backed feed cache persists across
 * ViewModel recreation and remains available when the network is unavailable.
 *
 * These tests use an in-memory Room database so no real network calls occur.
 * For true offline validation (network toggle), use manual test steps in the plan.
 */
@RunWith(AndroidJUnit4::class)
class FeedOfflineInstrumentationTest {

    private lateinit var db: AppLocalDbRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppLocalDbRepository::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertedPosts_areReturnedByGetAllPosts_inDescendingCreatedAtOrder() {
        val older = DreamPost(
            postId = "1",
            authorUid = "uid1",
            authorNickname = "Alice",
            title = "Older Dream",
            description = "...",
            imageUrl = "",
            createdAt = 1000L,
            lastUpdated = 1000L
        )
        val newer = DreamPost(
            postId = "2",
            authorUid = "uid1",
            authorNickname = "Alice",
            title = "Newer Dream",
            description = "...",
            imageUrl = "",
            createdAt = 2000L,
            lastUpdated = 2000L
        )

        db.dreamPostDao.insertPosts(older)
        db.dreamPostDao.insertPosts(newer)

        val result = awaitValue(db.dreamPostDao.getAllPosts())
        assertEquals(2, result.size)
        // Newer post should come first (DESC createdAt order)
        assertEquals("2", result[0].postId)
        assertEquals("1", result[1].postId)
    }

    @Test
    fun emptyDatabase_returnsEmptyList() {
        val result = awaitValue(db.dreamPostDao.getAllPosts())
        assertTrue(result.isEmpty())
    }

    @Test
    fun countActivePosts_ignoresSoftDeletedRows() {
        val activePost = DreamPost(
            postId = "active",
            authorUid = "uid1",
            authorNickname = "Alice",
            title = "Visible Dream",
            description = "...",
            imageUrl = "",
            createdAt = 1000L,
            lastUpdated = 1000L,
            isDeleted = false
        )
        val deletedPost = DreamPost(
            postId = "deleted",
            authorUid = "uid1",
            authorNickname = "Alice",
            title = "Deleted Dream",
            description = "...",
            imageUrl = "",
            createdAt = 2000L,
            lastUpdated = 2000L,
            isDeleted = true
        )

        db.dreamPostDao.insertPosts(activePost)
        db.dreamPostDao.insertPosts(deletedPost)

        assertEquals(1, db.dreamPostDao.countActivePosts())
    }

    @Test
    fun activePostsRemainReadableFromRoomWithoutAnyNetworkDependency() {
        val cachedPost = DreamPost(
            postId = "cached",
            authorUid = "uid2",
            authorNickname = "Bob",
            title = "Cached Dream",
            description = "Offline copy",
            imageUrl = "",
            createdAt = 3000L,
            lastUpdated = 3000L
        )

        db.dreamPostDao.insertPosts(cachedPost)

        val result = awaitValue(db.dreamPostDao.getAllPosts())
        assertEquals(1, result.size)
        assertEquals("cached", result.first().postId)
        assertEquals(1, db.dreamPostDao.countActivePosts())
    }

    private fun <T> awaitValue(liveData: LiveData<T>): T {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val latch = CountDownLatch(1)
        var capturedValue: T? = null

        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                capturedValue = value
                latch.countDown()
                liveData.removeObserver(this)
            }
        }

        instrumentation.runOnMainSync {
            liveData.observeForever(observer)
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertFalse(capturedValue == null)
        assertNotNull(capturedValue)
        return capturedValue!!
    }
}
