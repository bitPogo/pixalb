/*
 * Copyright (c) 2021 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by LGPL v2.1
 */

package io.bitpogo.gradle.pixalb.dependency

object Version {

    const val kotlin = "1.7.10"

    val gradle = Gradle
    val antibytes = Antibytes

    object Gradle {
        /**
         * [AnitBytes GradlePlugins](https://github.com/bitPogo/gradle-plugins)
         */
        const val antibytes = "009e2b6"

        /**
         * [Spotless](https://plugins.gradle.org/plugin/com.diffplug.gradle.spotless)
         */
        const val spotless = "6.9.0"
    }

    object Antibytes {
        const val test = "97a7c7a-bump-antibytes-SNAPSHOT"
        const val kfixture = "0.3.1"
        const val kmock = "0.3.0-rc02-bump-updates-1.7.10-SNAPSHOT"
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
        const val hilt = "2.38.1"

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
}
