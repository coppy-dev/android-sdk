import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.gradle.plugin-publish") version "1.2.0"
    id("signing")
}

kotlin {
    jvmToolchain(17)
}

group = "org.prototypic.coppy.generator"
version = "1.0.0-SNAPSHOT"

gradlePlugin {
    website.set("https://prototypic.org")
    vcsUrl.set("https://github.com/coppy-dev/android-sdk.git")

    plugins {
        create("generator") {
            id = "org.prototypic.coppy.generator"
            implementationClass = "org.prototypic.coppy.plugin.CoppyPlugin"
            displayName = "Coppy"
            description =
                "Plugin for Android application to generate Coppy content classes. Companion for the Coppy SDK."
            tags.set(listOf("android", "coppy", "content", "sdk"))
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("pluginMaven") {
            groupId = group.toString()

            pom {
                name.set("Coppy Android SDK")
                description.set("Coppy SDK: headles CMS for your app copy")
                url.set("https://coppy.app")
                inceptionYear.set("2023")
                developers {
                    developer {
                        name.set("Coppy Team")
                        organization.set("Prototypic")
                        email.set("coppy@prototypic.org")
                    }
                }
                organization {
                    name.set("Prototypic")
                    url.set("https://prototypic.org")
                }
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/coppy-dev/android-sdk/blob/main/LICENSE")
                    }
                }
                scm {
                    url.set("https://github.com/coppy-dev/android-sdk")
                    connection.set("https://github.com/coppy-dev/android-sdk.git")
                }
            }
        }
    }

    repositories {
        maven {
            name = "preview"
            url = uri("$buildDir/repo")
        }

        maven {
            name = "SonatypeOSS"
            credentials {
                username = project.property("ossrhUsername").toString()
                password = project.property("ossrhPassword").toString()
            }

            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }
    }
}

dependencies {
    implementation("com.android.tools.build:gradle:[7.3.1,)")
    implementation("org.json:json:[20180813,)")
    implementation("org.apache.commons:commons-text:[1.10.0,)")

    testImplementation(gradleTestKit())
    testImplementation("com.android.tools:common:31.1.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    environment("ANDROID_HOME", getSdkDir())
    useJUnitPlatform()
}

fun getSdkDir(): String {
    val androidHome = System . getenv ("ANDROID_HOME")
    if (androidHome != null) {
        return androidHome
    }
    val localProperties = Properties().apply {
        rootProject.file("local.properties").inputStream().use { load(it) }
    }
    return localProperties["sdk.dir"].toString()
}