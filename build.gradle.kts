// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    autowire(libs.plugins.com.android.application) apply false
    autowire(libs.plugins.kotlin.android) apply false
    autowire(libs.plugins.kotlin.kapt) apply false
    autowire(libs.plugins.kotlin.ksp) apply false
    autowire(libs.plugins.kotlin.serialization) apply false
    autowire(libs.plugins.kotlin.parcelize) apply false
    autowire(libs.plugins.hilt.android) apply false
    autowire(libs.plugins.com.squareup.wire) apply false

    autowire(libs.plugins.com.autonomousapps.dependency.analysis)
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}