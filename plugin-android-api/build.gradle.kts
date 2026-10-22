plugins { id("com.android.library"); kotlin("android") }
android {
    namespace = "com.kigtts.plugin.android"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlin { jvmToolchain(17) }
}
dependencies { api(project(":plugin-api")); api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2") }
