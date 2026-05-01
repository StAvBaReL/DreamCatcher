# Decision: Paging Feed Over Journal
## Date
2026-05-01

## Context
The merge being resolved contained two competing UI/data directions: an older journal-oriented `LiveData<List<DreamPost>>` flow and a newer feed/profile implementation using Paging 3, soft-delete support, and Room/Firebase sync cleanup.

## Decision
Keep the Paging-based feed/profile path as the active implementation. Preserve the global create action and profile sign-out navigation, and remove the obsolete `JournalViewModel` navigation path from the resolved code.

## Consequences
- Feed and profile stay aligned with the newer adapter/view holder behavior, including like, edit, and delete actions.
- The local DAO keeps a small raw-query compatibility surface for tests and profile counts.
- Future work should remove remaining journal-era resources if they are no longer needed anywhere else in the branch.

## Code references
- `app/src/main/java/com/colman/dreamcatcher/view/FeedAdapter.kt`
- `app/src/main/java/com/colman/dreamcatcher/view/FeedFragment.kt`
- `app/src/main/java/com/colman/dreamcatcher/viewmodel/FeedViewModel.kt`
- `app/src/main/res/navigation/nav_graph.xml`

## Last updated
2026-05-01
