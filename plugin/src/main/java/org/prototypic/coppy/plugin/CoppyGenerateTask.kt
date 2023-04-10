package org.prototypic.coppy.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.*
import java.net.URL

abstract class CoppyGenerateTask : DefaultTask() {

    @get:OutputDirectory
    abstract var outputDir: File
    @TaskAction
    fun taskAction() {
        val extension = project.extensions.getByType(CoppyPluginExtension::class.java)
        val spaceKey = extension.spaceKey

        if (spaceKey == null) {
            throw Exception("Cannot find correct space key. Make sure you pass it into plugin config in your project gradle.build")
        }

        val contentUrl = "https://content.coppy.app/${spaceKey}/content"
        println(contentUrl)
        val (content) = CoppyUtils.loadContent(URL(contentUrl))

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) throw Exception("Cannot create output directory ${outputDir.path}")
        }
        if (!outputDir.canWrite()) throw Exception("Cannot write into output directory ${outputDir.path}")

        val outputContentFile = File(outputDir, "Content.kt")
        if (outputContentFile.exists()) outputContentFile.delete()
        outputContentFile.writeText(CoppyContentCodeGenerator.generateContentFileContent(content))
    }
}