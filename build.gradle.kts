import godot.gradle.GodotLanguage

plugins {
    id("com.utopia-rise.godot-jvm") version "1.0.0-rc3"
}

repositories {
    mavenCentral()
}

godot {
    languages.set(setOf(GodotLanguage.KOTLIN))

    isGodotCoroutinesEnabled.set(true)
}

dependencies {
    implementation("org.jgrapht:jgrapht-core:1.5.3")
}
