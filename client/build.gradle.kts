/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

import tech.antibytes.gradle.configuration.runtime.AntiBytesTestConfigurationTask
import tech.antibytes.gradle.dependency.Dependency
import io.bitpogo.gradle.pixalb.dependency.Dependency as LocalDependency
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import tech.antibytes.gradle.configuration.isIdea

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")

    id("kotlin-parcelize")

    id("tech.antibytes.gradle.configuration")
    id("tech.antibytes.gradle.coverage")
    id("tech.antibytes.kmock.kmock-gradle")

    // Serialization
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    android()

    jvm()

    js(IR) {
        compilations {
            this.forEach {
                it.compileKotlinTask.kotlinOptions.sourceMap = true
                it.compileKotlinTask.kotlinOptions.metaInfo = true

                if (it.name == "main") {
                    it.compileKotlinTask.kotlinOptions.main = "call"
                }
            }
        }

        browser {
            testTask {
                useKarma {
                    useChromeHeadlessNoSandbox()
                }
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.serialization.InternalSerializationApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlinx.coroutines.DelicateCoroutinesApi")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(Dependency.multiplatform.kotlin.common)
                implementation(Dependency.multiplatform.coroutines.common)
                implementation(Dependency.multiplatform.ktor.common.client)
                implementation(Dependency.multiplatform.ktor.common.contentNegotiation)
                implementation(Dependency.multiplatform.ktor.common.json)
                implementation((Dependency.multiplatform.ktor.logger))

                implementation(Dependency.multiplatform.serialization.common)

                implementation(project(":coroutine-helper"))
            }
        }
        val commonTest by getting {
            kotlin.srcDir("${buildDir.absolutePath.trimEnd('/')}/generated/antibytes/commonTest/kotlin")

            dependencies {
                implementation(Dependency.multiplatform.test.common)
                implementation(Dependency.multiplatform.test.annotations)
                implementation(Dependency.multiplatform.ktor.mock)

                implementation(LocalDependency.antibytes.test.kmp.core)
                implementation(LocalDependency.antibytes.test.kmp.fixture)
                implementation(LocalDependency.antibytes.test.kmp.coroutine)
                implementation(LocalDependency.antibytes.test.kmp.ktor)

                implementation(LocalDependency.antibytes.test.kmp.kmock)
            }
        }

        val androidMain by getting {
            dependencies {
               implementation(Dependency.multiplatform.kotlin.android)
                implementation(Dependency.multiplatform.ktor.android.client)
            }
        }
        if (!isIdea()) {
            val androidAndroidTestRelease by getting
            val androidAndroidTest by getting {
                dependsOn(androidAndroidTestRelease)
            }
            val androidTestFixturesDebug by getting
            val androidTestFixturesRelease by getting

            val androidTestFixtures by getting {
                dependsOn(androidTestFixturesDebug)
                dependsOn(androidTestFixturesRelease)
            }

            val androidTest by getting {
                dependsOn(androidTestFixtures)
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(Dependency.multiplatform.test.jvm)
                implementation(Dependency.multiplatform.test.junit)
                implementation(Dependency.android.test.ktx)
                implementation(Dependency.android.test.robolectric)
                implementation(Dependency.android.test.junit)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(Dependency.multiplatform.kotlin.jdk8)
                implementation(Dependency.multiplatform.ktor.jvm.client)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(Dependency.multiplatform.test.jvm)
                implementation(Dependency.multiplatform.test.junit)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(Dependency.multiplatform.kotlin.js)
                implementation(Dependency.js.nodejs)
                implementation(Dependency.multiplatform.ktor.js.client)

            }
        }

        val jsTest by getting {
            dependencies {
                implementation(Dependency.multiplatform.test.js)
            }
        }
    }
}

android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kmock {
    rootPackage = "io.bitpogo.pixalb.client"
    freezeOnDefault = false
    allowInterfaces = true
}

tasks.withType(Test::class.java) {
    testLogging {
        events(FAILED)
    }
}

val apiKey: String = project.findProperty("gpr.pixabay.apikey").toString()

val provideTestConfig: Task by tasks.creating(AntiBytesTestConfigurationTask::class) {
    packageName.set("io.bitpogo.pixalb.client.test.config")
    this.stringFields.set(
        mapOf(
            "projectDir" to projectDir.toPath().toAbsolutePath().toString(),
            "apiKey" to apiKey,
        )
    )
}

tasks.withType(KotlinCompile::class.java) {
    if (this.name.contains("Test")) {
        this.dependsOn(provideTestConfig)
    }
}
