# Feed Sync And Profile Dreams
## Summary
The app feed and the profile dream list both read from the local Room database and use Firebase as the remote source for refreshes. The active UI path uses Paging 3 for list rendering, while a raw `LiveData<List<DreamPost>>` query is still kept for profile stats and test compatibility.

The refresh flow is delta-based. Posts updated after the last local sync are fetched from Firestore, written into Room, and then reconciled against the current remote post id set so deleted posts are removed locally.

## Key behavior
- Feed and profile lists page from Room rather than reading directly from Firestore.
- `LocalSyncManager` stores the last successful sync timestamp.
- Refresh also removes locally cached posts that are no longer active remotely.
- Profile stats use a non-paged Room query so counts can be derived locally.

## Code references
- `app/src/main/java/com/colman/dreamcatcher/model/DreamCatcherModel.kt` — repository orchestration for paging, sync, likes, add/update/delete.
- `app/src/main/java/com/colman/dreamcatcher/model/LocalSyncManager.kt` — persistent last-sync timestamp.
- `app/src/main/java/com/colman/dreamcatcher/model/dao/DreamPostDao.kt` — paged and raw Room queries used by feed/profile/tests.
- `app/src/main/java/com/colman/dreamcatcher/viewmodel/FeedViewModel.kt` — feed loading and like/delete actions.
- `app/src/main/java/com/colman/dreamcatcher/viewmodel/ProfileViewModel.kt` — profile paging plus local stats.

## Related
- [Decision: Paging Feed Over Journal](../decisions/paging-feed-over-journal.md)

## Notes
- Instrumentation tests still rely on `getAllPosts()` for local ordering checks even though the visible feed uses Paging.

## Last updated
2026-05-01
