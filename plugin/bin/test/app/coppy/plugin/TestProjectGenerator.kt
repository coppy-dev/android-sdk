package app.coppy.plugin

import java.io.File

@Suppress("unused")
class TestProjectGenerator(
    val projectDirectory: File,
) {

    val appDirectory = File(projectDirectory, "app")
    val appSrcDirectory = File(appDirectory, "src/main")
    val appManifestFile = File(appSrcDirectory, "AndroidManifest.xml")

    fun generate(
        appConfig: AppConfig?
    ) {
        appSrcDirectory.ensureDirectoryExist()

        generateSettings()
        generateGradleProperties()
        generateLocalProperties()
        generateApp(appConfig)
    }

    private fun generateGradleProperties() {
        File(projectDirectory, "gradle.properties").writeText(
            """
                android.useAndroidX=true
            """
        )
    }

    private fun generateSettings() {
        File(projectDirectory, "settings.gradle").writeText(
            """                
                pluginManagement {
                    repositories {
                        google()
                    }
                }
                
                rootProject.name = "project"
                include ':app'
                
                dependencyResolutionManagement {
                    repositories {
                        mavenCentral()
                        google()
                    }
                }
            """.trimIndent()
        )
    }

    private fun generateLocalProperties() {
        val localProperties = File(projectDirectory, "local.properties")
        val androidHome = System.getenv("ANDROID_HOME")
        localProperties.writeText("sdk.dir=$androidHome")
    }

    private fun generateApp(config: AppConfig?) {
        generateBuildScript(config)
        generateManifest()
    }

    private fun generateBuildScript(config: AppConfig?) {
        var gradleContent = """
            plugins {
                id 'com.android.application'
                id 'app.coppy'
            }
            
            android {
                namespace = "app.coppy.app"
                compileSdk = 33
            
                defaultConfig {
                    applicationId "app.coppy.app"
                    minSdk 21
                    targetSdk 33
                    versionCode 1
                    versionName "1.0"
                }
            }
            
        """.trimIndent()

        if (config != null) {
            gradleContent += """
                |coppy {
                |    contentKey = "${config.contentKey}"${if (config.updateType == null) "" else "\n|    updateType = \"${config.updateType}\""}${if (config.updateInterval == null) "" else "\n|    updateInterval = ${config.updateInterval}"}
                |}
            """.trimMargin()
        }
        File(appDirectory, "build.gradle").writeText(gradleContent)
    }

    private fun generateManifest() {
        appManifestFile.writeText(
            """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="org.prorotypic.app">
                    <application android:label="app">
                    </application>
                </manifest>
            """.trimIndent()
        )
    }

    class AppConfig(
        val contentKey: String,
        val updateType: String?,
        val updateInterval: Int?
    )
}