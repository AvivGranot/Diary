package com.proactivediary.data.sync

/**
 * Represents the current state of cloud sync operations.
 */
sealed class SyncState {
    /** No sync operation running */
    object Idle : SyncState()

    /** Pushing or pulling data */
    object Syncing : SyncState()

    /** An error occurred during sync */
    data class Error(val message: String) : SyncState()

    /** Restoring data from cloud (new device) */
    data class RestoreInProgress(val progress: Float) : SyncState()
}
