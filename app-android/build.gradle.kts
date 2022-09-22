/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */
import tech.antibytes.gradle.dependency.Version
import tech.antibytes.gradle.dependency.Dependency
import io.bitpogo.gradle.pixalb.dependency.Dependency as LocalDependency
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    id("kotlin-parcelize")

    id("tech.antibytes.gradle.configuration")
    id("tech.antibytes.gradle.coverage")
    id("tech.antibytes.kmock.kmock-gradle")

    // SqlDelight
    id("com.squareup.sqldelight")

    // Hilt
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

val apiKey: String = project.findProperty("gpr.pixabay.apikey").toString()


android {
    defaultConfig {
        applicationId = "io.bitpogo.pixabay.app"
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            "String",
            "API_KEY",
            "\"$apiKey\""
        )

        testInstrumentationRunner = "io.bitpogo.pixalb.app.HiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        debug {
            isMinifyEnabled = false
            matchingFallbacks.add("release")
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }


    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Version.android.compose.compiler
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(Dependency.android.ktx.core)
    implementation(Dependency.android.ktx.lifecycle)
    implementation(Dependency.android.ktx.viewmodel)
    implementation(Dependency.android.ktx.viewmodelCoroutine)

    implementation(Dependency.android.appCompact.core)
    implementation(Dependency.android.material)
    implementation(Dependency.android.constraintLayout)

    implementation(Dependency.android.compose.ui)
    implementation(Dependency.android.compose.material)
    implementation(Dependency.android.compose.materialIcons)
    implementation(Dependency.android.compose.materialIconsExtended)
    implementation(Dependency.android.compose.viewmodel)
    implementation(Dependency.android.compose.foundation)
    implementation(Dependency.android.compose.constrainLayout)
    implementation(Dependency.android.compose.navigation)

    implementation(LocalDependency.sqldelight.android)

    implementation(Dependency.multiplatform.dateTime)
    implementation(Dependency.multiplatform.serialization.android)
    implementation(Dependency.multiplatform.serialization.json)

    implementation(Dependency.multiplatform.ktor.logger)

    implementation(LocalDependency.google.hilt.composeNavigation)
    implementation(LocalDependency.google.hilt.core)
    kapt(LocalDependency.google.hilt.compiler)

    implementation(project(":coroutine-helper"))
    implementation(project(":client"))
    implementation(project(":album"))

    implementation(LocalDependency.coil.core)
    implementation(LocalDependency.coil.compose)

    // Debug
    debugImplementation(Dependency.android.compose.uiTooling)
    debugImplementation(Dependency.android.compose.uiManifest)

    // Test
    testImplementation(Dependency.android.test.junit)
    testImplementation(Dependency.android.test.junit4)
    testImplementation(Dependency.jvm.test.mockk.unit)
    testImplementation(Dependency.android.test.ktx)
    testImplementation(Dependency.android.test.composeJunit4)
    testImplementation(Dependency.android.test.robolectric)
    testImplementation(LocalDependency.antibytes.test.android.core)
    testImplementation(LocalDependency.antibytes.test.android.kmock)
    testImplementation(LocalDependency.antibytes.test.android.coroutine)
    testImplementation(LocalDependency.antibytes.test.android.fixture)
    testImplementation(Dependency.multiplatform.test.coroutines)

    testImplementation(LocalDependency.google.hilt.test)
    kaptTest(LocalDependency.google.hilt.compiler)

    // InstrumentedTest
    androidTestImplementation(Dependency.android.test.junit)
    androidTestImplementation(Dependency.android.test.junit4)
    androidTestImplementation(Dependency.android.test.composeJunit4)
    androidTestImplementation(Dependency.android.test.espressoCore)
    androidTestImplementation(Dependency.android.test.uiAutomator)

    androidTestImplementation(LocalDependency.antibytes.test.android.core)
    androidTestImplementation(LocalDependency.antibytes.test.android.kmock)
    androidTestImplementation(LocalDependency.antibytes.test.android.fixture)

    androidTestImplementation(LocalDependency.google.hilt.test)
    kaptAndroidTest(LocalDependency.google.hilt.compiler)
}

kmock {
    rootPackage = "io.bitpogo.pixalb.app"
    freezeOnDefault = false
    allowInterfaces = true
}

tasks.withType(Test::class.java) {
    testLogging {
        events(FAILED)
    }
}

sqldelight {
    database("PixabayDataBase") {
        packageName = "io.bitpogo.pixalb.store.database"
        sourceFolders = listOf("database")
        dependency(project(":album"))
        verifyMigrations = true
    }
}
