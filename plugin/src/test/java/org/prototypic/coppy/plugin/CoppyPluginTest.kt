package org.prototypic.coppy.plugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.Assertions.*
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants.*
import javax.xml.xpath.XPathFactory

class CoppyPluginTest {

    @TempDir
    lateinit var projectDir: File
    private lateinit var generator: TestProjectGenerator
    private lateinit var manifestFile: File
    private lateinit var updatedManifestFile: File

    var testSpaceKey = "9JS8jRHqyi4Vg0_DnqGTy"

    @BeforeEach
    fun setup() {
        projectDir.ensureDirectoryExist()
        generator = TestProjectGenerator(projectDir)
        updatedManifestFile = generator.appDirectory.resolve("build/intermediates/merged_manifests/debug/AndroidManifest.xml")
        manifestFile = generator.appManifestFile
    }

    @Test
    fun `Coppy Generator — throws an error if there is no coppy configuration in gradle build`() {
        generator.generate(null)

        val result = assembleTestApp(true)
        result.tasks.forEach { println(it.path) }
        assertEquals(TaskOutcome.FAILED, result.task(":app:generateCoppyContent")?.outcome)
    }

    @Test
    fun `Coppy Generatir — generates correct content file`() {
        generator.generate(
            TestProjectGenerator.AppConfig(testSpaceKey, null)
        )

        assembleTestApp()

        val contentFile = File(generator.appDirectory, "build/generated/source/coppy/Content.kt")
        assertTrue(contentFile.exists())

        val content = contentFile.readText()
        val expectedContent = """
            package org.prototypic.generatedCoppy
            
            import org.json.JSONObject
            import org.json.JSONArray
            import java.io.Serializable
            import org.prototypic.coppy.Updatable

            internal fun JSONObject.tryString(key: String): String? {
                val temp = this.optString(key)
                if (temp == "") return null
                return temp
            }
            internal fun JSONArray.tryString(key: Int): String? {
                val temp = this.optString(key)
                if (temp == "") return null
                return temp
            }
            @Suppress("unused")
            class CoppyContent: Serializable, Updatable {
                private var _value: String = "test"
                val value get() = _value

                override fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _value = obj.optString("value", _value)
                }
            }
        """.trimIndent()
        assertEquals(expectedContent, content)
    }

    @Test
    fun `Coppy Generator — removes content when cleaning the project`() {
        generator.generate(
            TestProjectGenerator.AppConfig(testSpaceKey, null)
        )

        assembleTestApp()

        val contentFile = File(generator.appDirectory, "build/generated/source/coppy/Content.kt")
        assertTrue(contentFile.exists())

        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("clean")
            .withPluginClasspath()
            .build()

        assertFalse(contentFile.exists())
    }
    @Test
    fun `Manifest updater — sets correct values of spaceKey and updateType in the manifest`() {
        generator.generate(
            TestProjectGenerator.AppConfig(testSpaceKey, updateType = "testUpdateType")
        )

        assembleTestApp()

        assertTrue(updatedManifestFile.exists())

        val (spaceKey, updateType) = getManifestValues(updatedManifestFile)
        assertEquals(testSpaceKey, spaceKey)
        assertEquals("testUpdateType", updateType)
    }

    @Test
    fun `Manifest updater — does not add "updateKey" if it is not set in gradle build`() {
        generator.generate(
            TestProjectGenerator.AppConfig(testSpaceKey, null)
        )

        assembleTestApp()

        assertTrue(updatedManifestFile.exists())

        val (spaceKey, updateType) = getManifestValues(updatedManifestFile)
        assertEquals(testSpaceKey, spaceKey)
        assertEquals(null, updateType)
    }

    private fun assembleTestApp(failureExpected: Boolean = false): BuildResult =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(":app:assembleDebug", "--info")
            .run {
                if (failureExpected) {
                    buildAndFail()
                } else {
                    build()
                }
            }

    private fun getManifestValues(manifestFile: File): Pair<String, String?> {
        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val updatedDoc = docBuilder.parse(manifestFile)

        val xPathFactory = XPathFactory.newInstance()
        val xPath = xPathFactory.newXPath()
        var spaceKeyPath =
            "/manifest/application/meta-data[@*='org.prototypic.coppy.spaceKey']/@*[name()='android:value']"
        val spaceKeyNode = xPath.evaluate(spaceKeyPath, updatedDoc, NODE) as Node
        val spaceKey = spaceKeyNode.textContent

        val updateTypePath = "/manifest/application/meta-data[@*='org.prototypic.coppy.updateType']/@*[name()='android:value']"
        val updateTypeNode = xPath.evaluate( updateTypePath, updatedDoc, NODE) as Node?
        val updateType = updateTypeNode?.textContent

        return Pair(spaceKey, updateType)
    }
}
