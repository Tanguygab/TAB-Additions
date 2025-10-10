plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.blossom)
}

group = "io.github.tanguygab"
version = "2.1.4"

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://nexus.scarsz.me/content/groups/public/")
    maven("https://m2.dv8tion.net/releases")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://repo.loohpjames.com/repository")
    maven("https://jitpack.io")
    maven("https://mvn.exceptionflug.de/repository/exceptionflug-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.spigot)
    compileOnly(libs.bungee)
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
    compileOnly(files("../../dependencies/TAB.jar"))

    compileOnly(libs.protocolize)
    compileOnly(libs.advancedban)

    compileOnly(libs.discordsrv)
    compileOnly(libs.essentials.discord)
    compileOnly(libs.interactivechat)

    compileOnly(libs.adventure.platform.bukkit)
    compileOnly(libs.adventure.platform.bungee)
    compileOnly(libs.adventure.minimessage)
    compileOnly(libs.adventure.serializer)
}

tasks.processResources {
    filesMatching(listOf("plugin.yml", "bungee.yml")) {
        expand(
            "version" to version,
            "kotlinVersion" to libs.versions.kotlin.get(),
            "adventurePlatformVersion" to libs.versions.adventure.platform.get(),
            "adventureMiniMessageVersion" to libs.versions.adventure.minimessage.get()
        )
    }
}

sourceSets {
    main {
        blossom {
            kotlinSources {
                property("version", version.toString())
            }
        }
    }
}