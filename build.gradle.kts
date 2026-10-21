import godot.gradle.GodotLanguage

plugins {
    id("com.utopia-rise.godot-jvm") version "1.0.0"
}

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

godot {
    languages.set(setOf(GodotLanguage.KOTLIN))

    isGodotCoroutinesEnabled.set(true)
}

dependencies {
    implementation("org.jgrapht:jgrapht-core:1.5.3")
    implementation("org.jgrapht:jgrapht-io:1.5.3")
}
