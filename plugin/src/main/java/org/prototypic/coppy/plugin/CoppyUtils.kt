package org.prototypic.coppy.plugin

import java.net.URL
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

@Suppress("unused")
object CoppyUtils {
    fun loadContent(url: URL): Pair<JSONObject, String> {
        try {
            val connection = url.openConnection()
            val input = connection.getInputStream()
            val reader = BufferedReader(InputStreamReader(input))
            val content = JSONObject(reader.readText())
            val eTag = connection.getHeaderField("Etag")
            reader.close()
            input.close()

            return Pair(content, eTag.replace("\"", ""))
        } catch (err: Exception) {
            throw Exception("Cannot load content from url. Make sure you added correct coppy content key in manifest file")
        }
    }
}