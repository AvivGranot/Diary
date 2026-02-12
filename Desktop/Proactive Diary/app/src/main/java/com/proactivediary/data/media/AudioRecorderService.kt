package com.proactivediary.data.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorderService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFilePath: String? = null

    private val audioDir: File
        get() = File(context.filesDir, "audio").also { it.mkdirs() }

    private fun entryAudioDir(entryId: String): File =
        File(audioDir, entryId).also { it.mkdirs() }

    /**
     * Starts recording audio for the given entry.
     * Returns the file path where audio will be saved.
     */
    suspend fun startRecording(entryId: String): String = withContext(Dispatchers.IO) {
        stopRecording() // Stop any existing recording

        val audioFile = File(entryAudioDir(entryId), "voice_note.m4a")
        currentFilePath = audioFile.absolutePath

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(currentFilePath)
            prepare()
            start()
        }

        currentFilePath!!
    }

    /**
     * Stops recording and returns the file path of the recorded audio.
     * Returns null if no recording was in progress.
     */
    suspend fun stopRecording(): String? = withContext(Dispatchers.IO) {
        val filePath = currentFilePath
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (_: Exception) {
                // Recording might have failed or was too short
            }
        }
        mediaRecorder = null
        currentFilePath = null
        filePath
    }

    /**
     * Deletes the audio file at the given path.
     */
    suspend fun deleteAudio(path: String) = withContext(Dispatchers.IO) {
        File(path).delete()
    }

    /**
     * Returns true if currently recording.
     */
    fun isRecording(): Boolean = mediaRecorder != null
}
