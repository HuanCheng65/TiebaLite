import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    autowire(libs.plugins.com.android.application)
    autowire(libs.plugins.kotlin.android)
    autowire(libs.plugins.kotlin.kapt)
    autowire(libs.plugins.kotlin.serialization)
    autowire(libs.plugins.kotlin.parcelize)
    autowire(libs.plugins.hilt.android)
    autowire(libs.plugins.kotlin.ksp)
    autowire(libs.plugins.com.squareup.wire)
}

val sha: String? = System.getenv("GITHUB_SHA")
val isCI: String? = System.getenv("CI")
val isSelfBuild = isCI.isNullOrEmpty() || !isCI.equals("true", ignoreCase = true)
val applicationVersionCode = property.versionCode
var applicationVersionName = property.versionName
val isPerVersion = property.isPreRelease
if (isPerVersion) {
    applicationVersionName += "-${property.preReleaseName}.${property.preReleaseVer}"
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
    buildToolsVersion = "34.0.0"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.huanchengfly.tieba.post"
        minSdk = 21
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = applicationVersionCode
        versionName = applicationVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders["is_self_build"] = "$isSelfBuild"
    }
    buildFeatures {
        compose = true
    }
    signingConfigs {
        if (property.keystore.file.isNotBlank()) {
            create("config") {
                storeFile = file(File(rootDir, property.keystore.file))
                storePassword = property.keystore.password
                keyAlias = property.keystore.key.alias
                keyPassword = property.keystore.key.password
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + project.buildDir.absolutePath + "/compose_metrics"
        )
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + project.buildDir.absolutePath + "/compose_metrics"
        )
        freeCompilerArgs += listOf(
            "-P", "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=" +
                    project.rootDir.absolutePath + "/compose_stability_configuration.txt"
        )
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "DebugProbesKt.bin"
        }
    }
    namespace = "com.huanchengfly.tieba.post"
    applicationVariants.configureEach {
        val variant = this
        outputs.configureEach {
            val fileName =
                "${variant.buildType.name}-${applicationVersionName}(${applicationVersionCode}).apk"

            (this as BaseVariantOutputImpl).outputFileName = fileName
        }
        kotlin.sourceSets {
            getByName(variant.name) {
                kotlin.srcDir("build/generated/ksp/${variant.name}/kotlin")
            }
        }
    }
}

dependencies {
    //Local Files
//    implementation fileTree(include: ["*.jar"], dir: "libs")

    implementation(net.swiftzer.semver.semver)
    implementation(godaddy.color.picker)

    implementation(airbnb.lottie)
    implementation(airbnb.lottie.compose)

    implementation(kotlinx.serialization.json)
    implementation(kotlinx.collections.immutable)

    implementation(androidx.media3.exoplayer)
    implementation(androidx.media3.ui)

    implementation(compose.destinations.core)
    ksp(compose.destinations.ksp)

    implementation(androidx.navigation.compose)

    api(wire.runtime)

    implementation(hilt.android)
    kapt(hilt.compiler)
    implementation(androidx.hilt.navigation.compose)
    kapt(androidx.hilt.compiler)

    implementation(accompanist.drawablepainter)
    implementation(accompanist.insets.ui)
    implementation(accompanist.systemuicontroller)
    implementation(accompanist.placeholder.material)

    implementation(sketch.core)
    implementation(sketch.compose)
    implementation(sketch.ext.compose)
    implementation(sketch.gif)
    implementation(sketch.okhttp)

    implementation(zoomimage.compose.sketch)

    implementation(compose.bom)
    androidTestImplementation(compose.bom)

    runtimeOnly(compose.runtime.tracing)
    implementation(compose.animation)
    implementation(compose.animation.graphics)
    implementation(compose.material)
    implementation(compose.material.icons.core)
    // Optional - Add full set of material icons
    implementation(compose.material.icons.extended)
    implementation(compose.ui.util)
//    implementation "androidx.compose.material3:material3"

    // Android Studio Preview support
    implementation(compose.ui.tooling.preview)
    debugImplementation(compose.ui.tooling)

    // UI Tests
    androidTestImplementation(compose.ui.test.junit4)
    debugRuntimeOnly(compose.ui.test.manifest)

    implementation(androidx.constraintlayout.compose)

    implementation(github.oaid)

    implementation(org.jetbrains.annotations)

    implementation(kotlin.stdlib)
    implementation(kotlin.reflect)

    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)

    implementation(androidx.lifecycle.runtime)
    implementation(androidx.lifecycle.viewmodel)
    implementation(androidx.lifecycle.viewmodel.compose)

    //AndroidX
    implementation(androidx.activity)
    implementation(androidx.activity.compose)
    implementation(androidx.appcompat)
    implementation(androidx.annotation)
    implementation(androidx.browser)
    implementation(androidx.constraintlayout)
    implementation(androidx.core)
    implementation(androidx.core.splashscreen)
    implementation(androidx.datastore.preferences)
    implementation(androidx.gridlayout)
    implementation(androidx.palette)
    implementation(androidx.window)
    implementation(androidx.startup.runtime)

    //Test
    testImplementation(junit.junit)
    androidTestImplementation(androidx.test.core)
    androidTestImplementation(androidx.test.ext.junit)
    androidTestImplementation(androidx.test.rules)
    androidTestImplementation(androidx.test.espresso.core)
    androidTestRuntimeOnly(androidx.test.runner)

    //Glide
    implementation(glide.core)
    ksp(glide.ksp)
    implementation(glide.okhttp3.integration)

    implementation(google.material)

    implementation(okhttp3.core)
    implementation(retrofit2.core)
    implementation(retrofit2.converter.wire)

    implementation(google.gson)
    implementation(org.litepal.android.kotlin)
    implementation(com.jaredrummler.colorpicker)

    implementation(github.matisse)
    implementation(xx.permissions)
    implementation(com.gyf.immersionbar.immersionbar)

    implementation(com.github.yalantis.ucrop)

    implementation(com.jakewharton.butterknife)
    kapt(com.jakewharton.butterknife.compiler)

    implementation(appcenter.analytics)
    implementation(appcenter.crashes)
    implementation(appcenter.distribute)
}
