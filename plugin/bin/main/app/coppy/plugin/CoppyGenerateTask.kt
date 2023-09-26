package app.coppy.plugin

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
        val contentKey = extension.contentKey

        if (contentKey == null) {
            throw Exception("Cannot find correct space key. Make sure you pass it into plugin config in your project gradle.build")
        }

        val contentUrl = "https://content.coppy.app/${contentKey}/content"
        val (content) = CoppyUtils.loadContent(URL(contentUrl))

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) throw Exception("Cannot create output directory ${outputDir.path}")
        }
        if (!outputDir.canWrite()) throw Exception("Cannot write into output directory ${outputDir.path}")

        val outputContentFile = File(outputDir, "Content.kt")
        if (outputContentFile.exists()) {
            if (content == null) {
                project.logger.info("Cannot load content from url. Make sure you added correct coppy content key in manifest file")
                project.logger.info("New content classes won't be generated. Existing content classes will be preserved.")
                // If we cannot load content from url, but there is an existing content,
                // it might mean that users accidentally put a wrong key, or
                // maybe they cancelled the subscription, or changed a key to a new one.
                // In any case we don't want to erase the content that is already in the project,
                // so we just return here and don't do anything.
                return
            }
            outputContentFile.delete()
        }
        if (content == null) {
            throw Exception("Cannot load content from url. Make sure you added correct coppy content key in manifest file")
        }
        outputContentFile.writeText(CoppyContentCodeGenerator.generateContentFileContent(content))
    }
}