/*
 * Copyright (c) 2022 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

import io.mockk.MockKGateway.Companion.implementation
import tech.antibytes.gradle.dependency.Dependency
import io.bitpogo.gradle.pixalb.dependency.Dependency as LocalDependency
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")

    id("kotlin-parcelize")

    id("tech.antibytes.gradle.configuration")
    id("tech.antibytes.gradle.coverage")
    id("tech.antibytes.kmock.kmock-gradle")

    // SqlDelight
    id("com.squareup.sqldelight")

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
                useMocha {
                    timeout = "5s"
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
                implementation((Dependency.multiplatform.ktor.logger))

                implementation(Dependency.multiplatform.koin.core.replace("3.2.0", "3.1.6"))

                implementation(Dependency.multiplatform.serialization.common)
                implementation(Dependency.multiplatform.serialization.json)

                implementation(Dependency.multiplatform.dateTime)

                implementation(LocalDependency.sqldelight.coroutines)

                implementation(project(":coroutine-helper"))
                implementation(project(":client"))
            }
        }
        val commonTest by getting {
            kotlin.srcDir("${buildDir.absolutePath.trimEnd('/')}/generated/antibytes/commonTest/kotlin")

            dependencies {
                implementation(Dependency.multiplatform.test.common)
                implementation(Dependency.multiplatform.test.annotations)

                implementation(LocalDependency.antibytes.test.kmp.core)
                implementation(LocalDependency.antibytes.test.kmp.annotations)
                implementation(LocalDependency.antibytes.test.kmp.fixture)
                implementation(LocalDependency.antibytes.test.kmp.coroutine)

                implementation(LocalDependency.antibytes.test.kmp.kmock)
            }
        }

        val androidMain by getting {
            dependencies {
               implementation(Dependency.multiplatform.kotlin.android)
                implementation(Dependency.multiplatform.coroutines.android)
            }
        }
        val androidAndroidTestRelease by getting
        val androidTestFixtures by getting
        val androidTestFixturesDebug by getting
        val androidTestFixturesRelease by getting
        val androidTest by getting {
            dependsOn(androidAndroidTestRelease)
            dependsOn(androidTestFixtures)
            dependsOn(androidTestFixturesDebug)
            dependsOn(androidTestFixturesRelease)

            dependencies {
                implementation(Dependency.multiplatform.test.jvm)
                implementation(Dependency.multiplatform.test.junit)
                implementation(Dependency.android.test.robolectric)
                implementation(Dependency.android.test.junit)
                implementation(LocalDependency.sqldelight.android)
                implementation(Dependency.android.test.ktx)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(Dependency.multiplatform.kotlin.jdk8)
                implementation(Dependency.multiplatform.coroutines.common)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(Dependency.multiplatform.test.jvm)
                implementation(Dependency.multiplatform.test.junit)
                implementation(LocalDependency.sqldelight.jvm)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(LocalDependency.sqldelight.js)
                implementation(Dependency.multiplatform.kotlin.js)
                implementation(Dependency.js.nodejs)
                implementation(devNpm("copy-webpack-plugin", "11.0.0"))
                implementation(npm("sql.js", "1.7.0"))
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
    rootPackage = "io.bitpogo.pixalb.album"
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
        packageName = "io.bitpogo.pixalb.album.database"
        sourceFolders = listOf("database")
        verifyMigrations = true
    }
}
