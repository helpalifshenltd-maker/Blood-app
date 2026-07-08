import java.util.Base64

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk = 35

  defaultConfig {
    applicationId = "my.alifbooldbank.com"
    minSdk = 21
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD") ?: "alifbloodbankpwd"
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD") ?: "alifbloodbankpwd"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  implementation(libs.play.services.ads)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

// Decode debug.keystore from debug.keystore.base64 if it does not exist
val keystoreFile = file("${rootDir}/debug.keystore")
val base64File = file("${rootDir}/debug.keystore.base64")
if (!keystoreFile.exists() && base64File.exists()) {
  try {
    val base64Content = base64File.readText().trim().replace("\\s".toRegex(), "")
    val decodedBytes = Base64.getDecoder().decode(base64Content)
    keystoreFile.writeBytes(decodedBytes)
    println("SUCCESS: Decoded debug.keystore from base64 at configuration time.")
  } catch (e: Exception) {
    println("ERROR: Failed to decode debug.keystore: ${e.message}")
  }
}

// Decode my-upload-key.jks from my-upload-key.jks.base64 if it does not exist
val uploadKeystoreFile = file("${rootDir}/my-upload-key.jks")
val uploadBase64File = file("${rootDir}/my-upload-key.jks.base64")
if (!uploadKeystoreFile.exists() && uploadBase64File.exists()) {
  try {
    val base64Content = uploadBase64File.readText().trim().replace("\\s".toRegex(), "")
    val decodedBytes = Base64.getDecoder().decode(base64Content)
    uploadKeystoreFile.writeBytes(decodedBytes)
    println("SUCCESS: Decoded my-upload-key.jks from base64 at configuration time.")
  } catch (e: Exception) {
    println("ERROR: Failed to decode my-upload-key.jks: ${e.message}")
  }
}

abstract class GenerateReleaseKeystoreTask : DefaultTask() {
  @get:OutputFile
  abstract val keystoreFile: RegularFileProperty

  @TaskAction
  fun generate() {
    val myKeystoreFile = keystoreFile.get().asFile
    if (!myKeystoreFile.exists()) {
      try {
        println("Generating release keystore at: ${myKeystoreFile.absolutePath}")
        val process = ProcessBuilder(
          "keytool", "-genkeypair",
          "-v",
          "-keystore", myKeystoreFile.absolutePath,
          "-storepass", "alifbloodbankpwd",
          "-keypass", "alifbloodbankpwd",
          "-alias", "upload",
          "-keyalg", "RSA",
          "-keysize", "2048",
          "-validity", "10000",
          "-dname", "CN=Alif Blood Bank, O=Official, C=BD"
        ).redirectErrorStream(true).start()
        
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        if (exitCode == 0) {
          println("SUCCESS: Release keystore generated successfully.")
        } else {
          println("ERROR: keytool failed with exit code $exitCode. Output:\n$output")
        }
      } catch (e: Exception) {
        println("ERROR: Failed to run keytool: ${e.message}")
      }
    } else {
      println("Keystore already exists, skipping generation.")
    }
  }
}

val generateReleaseKeystore = tasks.register<GenerateReleaseKeystoreTask>("generateReleaseKeystore") {
  keystoreFile.set(layout.projectDirectory.file("../my-upload-key.jks"))
}

tasks.named("preBuild") {
  dependsOn(generateReleaseKeystore)
}

abstract class CopyApkTask : DefaultTask() {
  @get:InputFile
  abstract val apkSource: RegularFileProperty

  @get:OutputFile
  abstract val apkDestination: RegularFileProperty

  @TaskAction
  fun copy() {
    val src = apkSource.get().asFile
    val dest = apkDestination.get().asFile
    if (src.exists()) {
      src.copyTo(dest, overwrite = true)
      val sizeInBytes = dest.length()
      val sizeInMb = sizeInBytes / (1024.0 * 1024.0)
      val formattedSize = String.format("%.2f", sizeInMb)
      println("SUCCESS: Compiled APK copied directly to: ${dest.absolutePath} (Size: $formattedSize MB)")
    } else {
      println("ERROR: Could not find compiled APK at: ${src.absolutePath}")
    }
  }
}

tasks.register<CopyApkTask>("copyApkToRoot") {
  dependsOn("assembleDebug")
  apkSource.set(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk"))
  apkDestination.set(layout.projectDirectory.file("../BloodConnectBD_debug.apk"))
}

abstract class CopyReleaseApkTask : DefaultTask() {
  @get:InputFile
  abstract val apkSource: RegularFileProperty

  @get:OutputFile
  abstract val apkDestination: RegularFileProperty

  @TaskAction
  fun copy() {
    val src = apkSource.get().asFile
    val dest = apkDestination.get().asFile
    if (src.exists()) {
      src.copyTo(dest, overwrite = true)
      val sizeInBytes = dest.length()
      val sizeInMb = sizeInBytes / (1024.0 * 1024.0)
      val formattedSize = String.format("%.2f", sizeInMb)
      println("SUCCESS: Release APK copied directly to: ${dest.absolutePath} (Size: $formattedSize MB)")
    } else {
      println("ERROR: Could not find compiled Release APK at: ${src.absolutePath}")
    }
  }
}

tasks.register<CopyReleaseApkTask>("copyReleaseApkToRoot") {
  dependsOn("assembleRelease")
  apkSource.set(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
  apkDestination.set(layout.projectDirectory.file("../AlifBloodBank_release.apk"))
}

abstract class CopyReleaseAabTask : DefaultTask() {
  @get:InputFile
  abstract val aabSource: RegularFileProperty

  @get:OutputFile
  abstract val aabDestination: RegularFileProperty

  @TaskAction
  fun copy() {
    val src = aabSource.get().asFile
    val dest = aabDestination.get().asFile
    if (src.exists()) {
      src.copyTo(dest, overwrite = true)
      val sizeInBytes = dest.length()
      val sizeInMb = sizeInBytes / (1024.0 * 1024.0)
      val formattedSize = String.format("%.2f", sizeInMb)
      println("SUCCESS: Release AAB copied directly to: ${dest.absolutePath} (Size: $formattedSize MB)")
    } else {
      println("ERROR: Could not find compiled Release AAB at: ${src.absolutePath}")
    }
  }
}

tasks.register<CopyReleaseAabTask>("copyReleaseAabToRoot") {
  dependsOn("bundleRelease")
  aabSource.set(layout.buildDirectory.file("outputs/bundle/release/app-release.aab"))
  aabDestination.set(layout.projectDirectory.file("../AlifBloodBank_release.aab"))
}




