import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.wire)
}

fun loadProperties(name: String): Properties = Properties().apply {
    val file = rootProject.file(name)
    if (file.exists()) file.inputStream().use { load(it) }
}

val applicationProperties = loadProperties("application.properties")
val keystoreProperties = loadProperties("keystore.properties")
val keystoreFile = keystoreProperties.getProperty("keystore.file", "")

val sha: String? = System.getenv("GITHUB_SHA")
val isCI: String? = System.getenv("CI")
val isSelfBuild = isCI.isNullOrEmpty() || !isCI.equals("true", ignoreCase = true)
val applicationVersionCode = applicationProperties.getProperty("versionCode").toInt()
var applicationVersionName: String = applicationProperties.getProperty("versionName")
val isPreRelease = applicationProperties.getProperty("isPreRelease").toBoolean()
if (isPreRelease) {
    applicationVersionName += "-${applicationProperties.getProperty("preReleaseName")}.${applicationProperties.getProperty("preReleaseVer")}"
}
if (!isSelfBuild && !sha.isNullOrEmpty()) {
    applicationVersionName += "+${sha.substring(0, 7)}"
}

wire {
    sourcePath {
        srcDir("src/main/protos")
    }

    kotlin {
        android = true
    }
}

android {
    compileSdk = 37
    defaultConfig {
        applicationId = "com.huanchengfly.tieba.post"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = applicationVersionCode
        versionName = applicationVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    signingConfigs {
        if (keystoreFile.isNotBlank()) {
            create("config") {
                storeFile = file(File(rootDir, keystoreFile))
                storePassword = keystoreProperties.getProperty("keystore.password")
                keyAlias = keystoreProperties.getProperty("keystore.key.alias")
                keyPassword = keystoreProperties.getProperty("keystore.key.password")
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }
    buildTypes {
        all {
            signingConfig =
                if (signingConfigs.any { it.name == "config" })
                    signingConfigs.getByName("config")
                else signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            isJniDebuggable = false
            multiDexEnabled = true
        }
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "DebugProbesKt.bin"
        }
    }
    namespace = "com.huanchengfly.tieba.post"
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            (output as VariantOutputImpl).outputFileName.set(
                "${variant.buildType}-${applicationVersionName}(${applicationVersionCode}).apk"
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_metrics")
    metricsDestination = layout.buildDirectory.dir("compose_metrics")
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_stability_configuration.txt"))
}

dependencies {
    //Local Files
//    implementation fileTree(include: ["*.jar"], dir: "libs")

    implementation(libs.godaddy.color.picker)

    implementation(libs.lottie)
    implementation(libs.lottie.compose)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    implementation(libs.compose.destinations.core)
    ksp(libs.compose.destinations.ksp)

    implementation(libs.androidx.navigation.compose)

    api(libs.wire.runtime)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.accompanist.drawablepainter)
    implementation(libs.accompanist.insets.ui)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.placeholder.material)

    implementation(libs.sketch.core)
    implementation(libs.sketch.compose)
    implementation(libs.sketch.extensions.compose)
    implementation(libs.sketch.gif)
    implementation(libs.sketch.okhttp)

    implementation(libs.zoomimage.compose.sketch)

    implementation(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))

    runtimeOnly(libs.compose.runtime.tracing)
    implementation(libs.compose.animation)
    implementation(libs.compose.animation.graphics)
    implementation(libs.compose.material)
    implementation(libs.compose.material.navigation)
    implementation(libs.compose.material.icons.core)
    // Optional - Add full set of material icons
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.util)
//    implementation "androidx.compose.material3:material3"

    // Android Studio Preview support
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // UI Tests
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugRuntimeOnly(libs.compose.ui.test.manifest)

    implementation(libs.androidx.constraintlayout.compose)

    implementation(libs.oaid)

    implementation(libs.jetbrains.annotations)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    //AndroidX
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.window)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.swiperefreshlayout)

    //Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestRuntimeOnly(libs.androidx.test.runner)

    //Glide
    implementation(libs.glide)
    ksp(libs.glide.ksp)
    implementation(libs.glide.okhttp3.integration)

    implementation(libs.google.material)

    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.wire)

    implementation(libs.gson)
    implementation(libs.litepal)
    implementation(libs.jaredrummler.colorpicker)

    implementation(libs.matisse)
    implementation(libs.xxpermissions)
    implementation(libs.immersionbar)

    implementation(libs.ucrop)
}
