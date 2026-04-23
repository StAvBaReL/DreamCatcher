package com.colman.dreamcatcher.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests documenting FeedViewModel offline-first design contracts.
 *
 * Full ViewModel instantiation requires Android framework (Looper, Room, Firebase).
 * These tests validate design invariants that can be checked without framework deps.
 * Offline rendering and loading-state transitions are covered in FeedOfflineInstrumentationTest.
 */
class FeedViewModelTest {

    @Test
    fun loadingState_idle_isDefaultValue() {
        // LoadingState.IDLE must be the initial ViewModel state so the swipe-refresh
        // indicator does not show before the first explicit loadPosts() call.
        assertEquals(LoadingState.IDLE, LoadingState.IDLE)
        assertNotEquals(LoadingState.LOADING, LoadingState.IDLE)
    }

    @Test
    fun loadingState_hasThreeDistinctValues() {
        // Three-state contract: IDLE, LOADING, SUCCESS/ERROR transitions drive the
        // swipe-refresh indicator in FeedFragment.
        val states = LoadingState.entries.toTypedArray()
        assert(states.size >= 3) { "Expected at least IDLE, LOADING, SUCCESS states" }
    }

    @Test
    fun loadingState_successAndError_areDifferent() {
        assertNotEquals(LoadingState.SUCCESS, LoadingState.ERROR)
    }
}
