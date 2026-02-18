package com.proactivediary.data.sync

/**
 * Sync status constants for Room entities.
 * Tracks whether a local row has been pushed to Firestore.
 */
object SyncStatus {
    /** Successfully synced to Firestore */
    const val SYNCED = 0
    /** Local change pending upload to Firestore */
    const val PENDING_UPLOAD = 1
    /** Marked for deletion — hidden from UI, hard-deleted after cloud confirms */
    const val PENDING_DELETE = 2
}
