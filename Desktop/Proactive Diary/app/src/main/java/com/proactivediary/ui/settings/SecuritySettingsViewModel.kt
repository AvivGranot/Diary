package com.proactivediary.ui.settings

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.proactivediary.data.db.dao.EntryDao
import com.proactivediary.data.db.dao.PreferenceDao
import com.proactivediary.data.db.entities.PreferenceEntity
import com.proactivediary.data.media.ImageStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val entryDao: EntryDao,
    private val imageStorageManager: ImageStorageManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isLockEnabled = MutableStateFlow(false)
    val isLockEnabled: StateFlow<Boolean> = _isLockEnabled

    private val _isHideContentEnabled = MutableStateFlow(false)
    val isHideContentEnabled: StateFlow<Boolean> = _isHideContentEnabled

    private val _canUseBiometric = MutableStateFlow(false)
    val canUseBiometric: StateFlow<Boolean> = _canUseBiometric

    init {
        checkBiometricCapability()
        loadPreferences()
    }

    private fun checkBiometricCapability() {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        _canUseBiometric.value = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _isLockEnabled.value = preferenceDao.get("app_lock_enabled")?.value == "true"
            _isHideContentEnabled.value = preferenceDao.get("hide_in_switcher")?.value == "true"
        }
    }

    fun toggleLock(enabled: Boolean) {
        _isLockEnabled.value = enabled
        viewModelScope.launch {
            preferenceDao.insert(
                PreferenceEntity("app_lock_enabled", if (enabled) "true" else "false")
            )
        }
    }

    fun toggleHideContent(enabled: Boolean) {
        _isHideContentEnabled.value = enabled
        viewModelScope.launch {
            preferenceDao.insert(
                PreferenceEntity("hide_in_switcher", if (enabled) "true" else "false")
            )
        }
    }

    fun exportAllAsZip() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val entries = entryDao.getAllSync()
                    if (entries.isEmpty()) return@withContext

                    val gson = com.google.gson.Gson()
                    val json = gson.toJson(entries)

                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, "proactive_diary_backup.zip")
                        put(MediaStore.Downloads.MIME_TYPE, "application/zip")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val uri = context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                    ) ?: return@withContext

                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        ZipOutputStream(BufferedOutputStream(os)).use { zip ->
                            // Add entries JSON
                            zip.putNextEntry(ZipEntry("entries.json"))
                            zip.write(json.toByteArray())
                            zip.closeEntry()

                            // Add images
                            val imagesDir = File(context.filesDir, "entry_images")
                            if (imagesDir.exists()) {
                                imagesDir.walkTopDown().filter { it.isFile }.forEach { file ->
                                    val relativePath = file.relativeTo(context.filesDir).path
                                    zip.putNextEntry(ZipEntry(relativePath))
                                    file.inputStream().use { it.copyTo(zip) }
                                    zip.closeEntry()
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Silent fail — export is best-effort
                }
            }
        }
    }

    fun deleteAllCloudData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val user = FirebaseAuth.getInstance().currentUser ?: return@withContext
                    val db = FirebaseFirestore.getInstance()
                    val userDoc = db.collection("users").document(user.uid)

                    // Delete all subcollections (entries, settings, etc.)
                    val subcollections = listOf("entries", "settings", "backups")
                    for (collection in subcollections) {
                        val snapshot = userDoc.collection(collection).get().result
                        snapshot?.documents?.forEach { doc ->
                            doc.reference.delete()
                        }
                    }

                    // Delete user document
                    userDoc.delete()
                } catch (_: Exception) {
                    // Silent fail — cloud deletion is best-effort
                }
            }
        }
    }
}
