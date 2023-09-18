package app.coppy.plugin

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

    var testContentKey = "9JS8jRHqyi4Vg0_DnqGTy"

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
    fun `Coppy Generator — generates correct content file`() {
        generator.generate(
            TestProjectGenerator.AppConfig(testContentKey, null, null)
        )

        assembleTestApp()

        val contentFile = File(generator.appDirectory, "build/generated/source/coppy/Content.kt")
        assertTrue(contentFile.exists())

        val content = contentFile.readText()
        val expectedContent = """
            package app.coppy.generatedCoppy
            
            import org.json.JSONObject
            import org.json.JSONArray
            import java.io.Serializable
            import app.coppy.Updatable

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
            TestProjectGenerator.AppConfig(testContentKey, null, null)
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
    fun `Manifest updater — sets correct values for provided props in the manifest`() {
        generator.generate(
            TestProjectGenerator.AppConfig(testContentKey, updateType = "testUpdateType", updateInterval = 15)
        )

        assembleTestApp()

        assertTrue(updatedManifestFile.exists())

        val (contentKey, updateType, updateInterval) = getManifestValues(updatedManifestFile)
        assertEquals(testContentKey, contentKey)
        assertEquals("testUpdateType", updateType)
        assertEquals(15, updateInterval)
    }

    @Test
    fun `Manifest updater — does not add "updateKey" if it is not set in gradle build`() {
        generator.generate(
            TestProjectGenerator.AppConfig(testContentKey, null, null)
        )

        assembleTestApp()

        assertTrue(updatedManifestFile.exists())

        val (contentKey, updateType, updateInterval) = getManifestValues(updatedManifestFile)
        assertEquals(testContentKey, contentKey)
        assertEquals(30, updateInterval)
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

    private fun getManifestValues(manifestFile: File): Triple<String?, String?, Int?> {
        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val updatedDoc = docBuilder.parse(manifestFile)

        val xPathFactory = XPathFactory.newInstance()
        val xPath = xPathFactory.newXPath()
        var contentKeyPath =
            "/manifest/application/meta-data[@*='app.coppy.contentKey']/@*[name()='android:value']"
        val contentKeyNode = xPath.evaluate(contentKeyPath, updatedDoc, NODE) as Node?
        val contentKey = contentKeyNode?.textContent

        val updateTypePath = "/manifest/application/meta-data[@*='app.coppy.updateType']/@*[name()='android:value']"
        val updateTypeNode = xPath.evaluate( updateTypePath, updatedDoc, NODE) as Node?
        val updateType = updateTypeNode?.textContent

        val updateIntervalPath = "/manifest/application/meta-data[@*='app.coppy.updateInterval']/@*[name()='android:value']"
        val updateIntervalNode = xPath.evaluate(updateIntervalPath, updatedDoc, NODE) as Node?
        val updateInterval = updateIntervalNode?.textContent?.toInt() ?: 0

        return Triple(contentKey, updateType, updateInterval)
    }
}
