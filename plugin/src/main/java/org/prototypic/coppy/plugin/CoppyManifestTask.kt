package org.prototypic.coppy.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.w3c.dom.Node
import java.io.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

abstract class CoppyManifestTask : DefaultTask() {
    @get:InputFile
    abstract val mergedManifest: RegularFileProperty

    @get:OutputFile
    abstract val updatedManifest: RegularFileProperty

    @TaskAction
    fun taskAction() {
        val extension = project.extensions.getByType(CoppyPluginExtension::class.java)
        val spaceKey = extension.spaceKey
        val updateType = extension.updateType
        val updateInterval = extension.updateInterval

        if (spaceKey == null) {
            throw GradleException("Cannot find correct space key. Make sure you pass it into plugin config in your project gradle.build")
        }

        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(mergedManifest.get().asFile)

            val xPathFactory = XPathFactory.newInstance()
            val xPath = xPathFactory.newXPath()
            var path =
                "/manifest/application"
            val applicationItem = xPath.evaluate(path, doc, XPathConstants.NODE) as Node

            // Space key
            val spaceKeyElement = doc.createElement("meta-data")
            spaceKeyElement.setAttribute("android:name", "org.prototypic.coppy.spaceKey")
            spaceKeyElement.setAttribute("android:value", spaceKey)
            applicationItem.appendChild(spaceKeyElement)

            // Update interval
            val updateIntervalElement = doc.createElement("meta-data")
            updateIntervalElement.setAttribute("android:name", "org.prototypic.coppy.updateInterval")
            updateIntervalElement.setAttribute("android:value", updateInterval?.toString()?: "30")
            applicationItem.appendChild(updateIntervalElement)

            // Update type
            if (updateType != null) {
                val updateTypeElement = doc.createElement("meta-data")
                updateTypeElement.setAttribute("android:name", "org.prototypic.coppy.updateType")
                updateTypeElement.setAttribute("android:value", updateType)
                applicationItem.appendChild(updateTypeElement)
            }

            val output = FileOutputStream(updatedManifest.get().asFile)
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

            transformer.transform(DOMSource(doc), StreamResult(output))
        } catch (err: Error) {
            throw GradleException("Cannot update manifets file.")
        }
    }
}