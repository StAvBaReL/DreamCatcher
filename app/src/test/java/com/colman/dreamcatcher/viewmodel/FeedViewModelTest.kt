package com.colman.dreamcatcher.viewmodel

import com.colman.dreamcatcher.model.resolveSyncStartTimestamp
import com.colman.dreamcatcher.model.shouldForceFullSync
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedViewModelTest {

    @Test
    fun emptyCache_forcesFullSync_whenLastSyncIsZero() {
        assertTrue(shouldForceFullSync(localActivePostCount = 0))
        assertEquals(0L, resolveSyncStartTimestamp(localActivePostCount = 0, lastSyncTimestamp = 0L))
    }

    @Test
    fun emptyCache_forcesFullSync_whenLastSyncIsNonZero() {
        assertTrue(shouldForceFullSync(localActivePostCount = 0))
        assertEquals(0L, resolveSyncStartTimestamp(localActivePostCount = 0, lastSyncTimestamp = 1234L))
    }

    @Test
    fun populatedCache_preservesExplicitFullSyncStart() {
        assertFalse(shouldForceFullSync(localActivePostCount = 3))
        assertEquals(0L, resolveSyncStartTimestamp(localActivePostCount = 3, lastSyncTimestamp = 0L))
    }

    @Test
    fun populatedCache_preservesDeltaSyncTimestamp() {
        assertFalse(shouldForceFullSync(localActivePostCount = 5))
        assertEquals(9876L, resolveSyncStartTimestamp(localActivePostCount = 5, lastSyncTimestamp = 9876L))
    }

    @Test
    fun loadingState_successAndError_areDifferent() {
        assertNotEquals(LoadingState.SUCCESS, LoadingState.ERROR)
    }
}
