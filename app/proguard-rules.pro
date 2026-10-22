# Behpay ProGuard / R8 Rules for Release Builds

# SQLDelight
-keep class ir.behpay.database.** { *; }
-keep interface ir.behpay.database.** { *; }
-keep class app.cash.sqldelight.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Koin Dependency Injection
-keep class io.insert.koin.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Domain Models & Navigation routes
-keep class ir.behpay.core.model.** { *; }
-keep class ir.behpay.core.notification.** { *; }
-keep class ir.behpay.navigation.** { *; }

# Kotlinx Coroutines & DateTime
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.datetime.** { *; }
