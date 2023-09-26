plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
}

group = "app.coppy"
version = "1.0.0"

android {
    namespace = "app.coppy"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
            afterEvaluate {
                from(components["release"])
            }

            artifactId = "core"
            groupId = "${project.group}"
            version = "${project.version}"

            pom {
                name.set("Coppy Android SDK")
                description.set("Coppy SDK: headles CMS for your app copy")
                url.set("https://coppy.app")
                inceptionYear.set("2023")
                developers {
                    developer {
                        name.set("Coppy Team")
                        organization.set("Coppy")
                        email.set("team@coppy.app")
                    }
                }
                organization {
                    name.set("Coppy")
                    url.set("https://coppy.app")
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

            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }
    }
}

signing {
    sign(publishing.publications["release"])
}







