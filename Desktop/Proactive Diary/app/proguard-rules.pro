# Proactive Diary ProGuard Rules

# ─── Room ───
-keep class com.proactivediary.data.db.entities.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# ─── Hilt ───
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ─── Gson ───
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ─── Firebase ───
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ─── Firebase Auth ───
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# ─── Google Credentials / Sign-In ───
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn androidx.credentials.**

# ─── Google Play Billing ───
-keep class com.android.billingclient.api.** { *; }
-keep class com.android.vending.billing.** { *; }
-dontwarn com.android.billingclient.**

# ─── Coil ───
-keep class coil.** { *; }
-dontwarn coil.**

# ─── OkHttp (Coil dependency) ───
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ─── Coroutines ───
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ─── Kotlin Serialization (keep metadata) ───
-keepattributes RuntimeVisibleAnnotations
-keep class kotlin.Metadata { *; }

# ─── App domain models (used in JSON conversion) ───
-keep class com.proactivediary.domain.model.** { *; }
-keep class com.proactivediary.data.db.converters.** { *; }

# ─── Firebase Cloud Functions ───
-keep class com.google.firebase.functions.** { *; }
-dontwarn com.google.firebase.functions.**
