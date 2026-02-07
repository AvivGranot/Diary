# Proactive Diary ProGuard Rules

# Keep Room entities
-keep class com.proactivediary.data.db.entities.** { *; }

# Keep Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
