/*
 * Copyright (c) 2021 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by LGPL v2.1
 */

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

// To make it available as direct dependency
group = "io.bitpogo.gradle.pixalb.dependency"
version = "1.0.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

gradlePlugin {
    plugins.register("io.bitpogo.gradle.pixalb.dependency") {
        id = "io.bitpogo.gradle.pixalb.dependency"
        implementationClass = "io.bitpogo.gradle.pixalb.dependency.DependencyPlugin"
    }
}
