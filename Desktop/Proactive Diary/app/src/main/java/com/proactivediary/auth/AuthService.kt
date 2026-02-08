package com.proactivediary.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    val isAuthenticated: Boolean
        get() = auth.currentUser != null

    val userEmail: String?
        get() = auth.currentUser?.email

    val userDisplayName: String?
        get() = auth.currentUser?.displayName

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    /**
     * Launches Google Sign-In via Credential Manager.
     * Requires a valid WEB_CLIENT_ID from Firebase Console.
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken, null
                )
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                    ?: return Result.failure(Exception("Sign-in succeeded but user is null"))
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Unexpected credential type"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create account with email and password.
     */
    suspend fun createAccountWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
                ?: return Result.failure(Exception("Account created but user is null"))
            result.user?.sendEmailVerification()?.await()
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in with email and password.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
                ?: return Result.failure(Exception("Sign-in succeeded but user is null"))
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    companion object {
        const val WEB_CLIENT_ID = "434907129966-s3rb5i0lgntm82pd6bii1k84vool7rni.apps.googleusercontent.com"
    }
}
