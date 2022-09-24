/*
 * Copyright (c) 2021 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by LGPL v2.1
 */

package io.bitpogo.gradle.pixalb.dependency

object Version {

    const val kotlin = "1.7.10"

    val gradle = Gradle

    object Gradle {
        /**
         * [Kotlin](https://github.com/JetBrains/kotlin)
         */
        const val kotlin = Version.kotlin

        /**
         * [AnitBytes GradlePlugins](https://github.com/bitPogo/gradle-plugins)
         */
        const val antibytes = "9e2ffe9"

        /**
         * [Spotless](https://plugins.gradle.org/plugin/com.diffplug.gradle.spotless)
         */
        const val spotless = "6.11.0"
    }

    val antibytes = Antibytes

    object Antibytes {
        const val test = "9401af5"
        const val kfixture = "0.3.1"
        const val kmock = "0.3.0-rc04"
    }

    /**
     * [SQLDelight](https://github.com/cashapp/sqldelight/)
     */
    const val sqldelight = "1.5.3"

    val google = Google

    object Google {
        /**
         * [Google Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
         */
        const val hilt = "2.43.2"

        /**
         * [Google Hilt Compose](https://developer.android.com/jetpack/androidx/releases/hilt)
         */
        const val hiltCompose = "1.0.0"
    }

    val androidx = AndroidX

    object AndroidX {
        /**
         * [Annotation](https://developer.android.com/jetpack/androidx/releases/annotation)
         */
        const val annotation = "1.4.0"
    }

    val jvm = Jvm

    object Jvm {
        /**
         * [Log4J](http://www.slf4j.org/)
         */
        const val slf4j = "1.7.36"
    }

    /**
     * [Coil](https://github.com/coil-kt/coil)
     */
    const val coil = "2.1.0"

    /**
     * [Paparazzi](https://github.com/cashapp/paparazzi)
     */
    const val paparazzi = "1.0.0"
}
