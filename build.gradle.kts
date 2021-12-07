import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    kotlin("jvm") version "1.6.0"
    id("org.jetbrains.intellij") version "1.3.0"
}

group = "cn.yiiguxing.plugin.figlet"
version = properties("version")

repositories {
    maven(url = "https://maven.aliyun.com/repository/public")
    maven(url = "https://maven-central.storage-download.googleapis.com/repos/central/data/")
    maven(url = "https://repo.eclipse.org/content/groups/releases/")
    maven(url = "https://www.jetbrains.com/intellij-repository/releases")
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("com.github.dtmo.jfiglet:jfiglet:1.0.1")
}

intellij {
    pluginName.set("FIGlet")
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
}

tasks {

    patchPluginXml {
        version.set(properties("version"))
        sinceBuild.set(properties("customSinceBuild"))
        untilBuild.set(properties("customUntilBuild"))
    }

    buildSearchableOptions {
        enabled = false
    }

    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}