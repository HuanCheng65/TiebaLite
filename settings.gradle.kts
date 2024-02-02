pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/jcenter")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }
}
plugins {
    id("com.highcapable.sweetdependency") version "1.0.4"
    id("com.highcapable.sweetproperty") version "1.0.5"
}
sweetProperty {
    isEnable = true
    global {
        all {
            isEnableTypeAutoConversion = true
            propertiesFileNames(
                "keystore.properties",
                "application.properties",
                isAddDefault = true
            )
            permanentKeyValues(
                "releaseKeyStore" to ""
            )
            generateFrom(CURRENT_PROJECT, ROOT_PROJECT)
        }
        buildScript {
            extensionName = "property"
        }
    }
}

rootProject.name = "TiebaLite"
include(":app")
