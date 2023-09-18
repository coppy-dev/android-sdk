plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
}

group = "org.prototypic.coppy"
version = "1.0.0-SNAPSHOT"

android {
    namespace = "org.prototypic.coppy"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-process:[2.6.1,)")
    implementation("androidx.work:work-runtime-ktx:[2.8.1,)")
    implementation("androidx.compose.ui:ui:[1.4.1,)")
    implementation("androidx.core:core-ktx:[1.10.0,)")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            artifactId = "core"

            afterEvaluate {
                from(components["release"])
            }

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
            url = uri("${project.buildDir}/repo")
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

signing {
    sign(publishing.publications["release"])
}






