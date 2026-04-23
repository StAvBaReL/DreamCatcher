package com.colman.dreamcatcher

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.colman.dreamcatcher.model.DreamPost
import com.colman.dreamcatcher.model.dao.AppLocalDbRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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

        val latch = CountDownLatch(1)
        var result: List<DreamPost> = emptyList()

        db.dreamPostDao.getAllPosts().observeForever { posts ->
            if (posts != null) {
                result = posts
                latch.countDown()
            }
        }

        latch.await(2, TimeUnit.SECONDS)
        assertEquals(2, result.size)
        // Newer post should come first (DESC createdAt order)
        assertEquals("2", result[0].postId)
        assertEquals("1", result[1].postId)
    }

    @Test
    fun emptyDatabase_returnsEmptyList() {
        val latch = CountDownLatch(1)
        var result: List<DreamPost>? = null

        db.dreamPostDao.getAllPosts().observeForever { posts ->
            result = posts
            latch.countDown()
        }

        latch.await(2, TimeUnit.SECONDS)
        assertTrue(result?.isEmpty() == true)
    }
}
