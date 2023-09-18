package app.coppy.plugin

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import java.io.File

@Suppress("unused")
class CoppyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("coppy", CoppyPluginExtension::class.java)

        val mainSourceSet = project.extensions.getByType(CommonExtension::class.java).sourceSets.getByName("main")
        processSourceSet(project, mainSourceSet)

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            val manifestUpdater = project.tasks.register(
                variant.name + "CoppyManifestUpdater",
                CoppyManifestTask::class.java
            )

            variant.artifacts.use(manifestUpdater)
                .wiredWithFiles(
                    CoppyManifestTask::mergedManifest,
                    CoppyManifestTask::updatedManifest
                )
                .toTransform(SingleArtifact.MERGED_MANIFEST)
        }
    }

    private fun processSourceSet(project: Project, sourceSet: AndroidSourceSet) {
        val generateTask = project.tasks.create("generateCoppyContent", CoppyGenerateTask::class.java) { task ->
            task.outputDir = File(project.buildDir, "generated/source/coppy")
            task.outputs.upToDateWhen { false }
        }

        project.dependencies.add(sourceSet.implementationConfigurationName,
            project.files({ generateTask.outputDir }) { it.builtBy(generateTask) })

        // Late bind the actual output directory
        sourceSet.java.srcDir({ generateTask.outputDir })

        project.afterEvaluate {
            (project.tasks.getByName("clean") as? Delete)?.let { cleanTask ->
                cleanTask.delete(generateTask.outputDir)
            }
        }
    }
}
internal fun File.ensureDirectoryExist() {
    if (!exists()) {
        if (!mkdirs()) {
            error("Failed to create dirs for '${this}'")
        }
    }
}