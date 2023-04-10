package org.prototypic.coppy.plugin

import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CoppyParserTest {
    @Test
    fun `parser — parse simple object`() {
        val obj = JSONObject()
            .put("header", "Header")
            .put("body", "Body text")
            .put("optional", "")

        val cl = CoppyParser.parseJson(obj, "CoppyContent")

        assertEquals("CoppyContent", cl.name)
        assertEquals(3, cl.fields.size)

        assertEquals("header", cl.fields.get(0).key)
        assertEquals("string", cl.fields.get(0).type)
        assertEquals(false, cl.fields.get(0).optional)
        assertEquals(null, cl.fields.get(0).cl)

        assertEquals("body", cl.fields.get(2).key)
        assertEquals("string", cl.fields.get(2).type)
        assertEquals(false, cl.fields.get(2).optional)
        assertEquals(null, cl.fields.get(2).cl)

        assertEquals("optional", cl.fields.get(1).key)
        assertEquals("string", cl.fields.get(1).type)
        assertEquals(true, cl.fields.get(1).optional)
        assertEquals(null, cl.fields.get(1).cl)
    }

    @Test
    fun `parser — parse simple object with not supported type`() {
        val obj = JSONObject()
            .put("header", "Header")
            .put("body", "Body text")
            .put("check", true)

        val cl = CoppyParser.parseJson(obj, "CoppyContent")

        assertEquals("CoppyContent", cl.name)
        assertEquals(2, cl.fields.size)

        assertEquals("header", cl.fields.get(0).key)
        assertEquals("string", cl.fields.get(0).type)
        assertEquals(false, cl.fields.get(0).optional)
        assertEquals(null, cl.fields.get(0).cl)

        assertEquals("body", cl.fields.get(1).key)
        assertEquals("string", cl.fields.get(1).type)
        assertEquals(false, cl.fields.get(1).optional)
        assertEquals(null, cl.fields.get(1).cl)
    }

    @Test
    fun `parser — parse nested object`() {
        val obj = JSONObject()
            .put("header", "Header")
            .put("body", "Body text")
            .put("link", JSONObject()
                .put("title", "title")
                .put("url", "url")
            )

        val cl = CoppyParser.parseJson(obj, "CoppyContent")

        assertEquals("CoppyContent", cl.name)
        assertEquals(3, cl.fields.size)

        assertEquals("header", cl.fields.get(1).key)
        assertEquals("string", cl.fields.get(1).type)
        assertEquals(false, cl.fields.get(1).optional)
        assertEquals(null, cl.fields.get(1).cl)

        assertEquals("body", cl.fields.get(2).key)
        assertEquals("string", cl.fields.get(2).type)
        assertEquals(false, cl.fields.get(2).optional)
        assertEquals(null, cl.fields.get(2).cl)

        val linkField = cl.fields.get(0)
        assertEquals("link", linkField.key)
        assertEquals("object", linkField.type)
        assertEquals(false, linkField.optional)
        val linkClass = linkField.cl
        assertNotNull(linkClass)
        assertEquals("CoppyContentLink", linkClass?.name)
        assertEquals(2, linkClass?.fields?.size)

        assertEquals("title", linkClass?.fields?.get(0)?.key)
        assertEquals("string", linkClass?.fields?.get(0)?.type)
        assertEquals(false, linkClass?.fields?.get(0)?.optional)
        assertEquals(null, linkClass?.fields?.get(0)?.cl)

        assertEquals("url", linkClass?.fields?.get(1)?.key)
        assertEquals("string", linkClass?.fields?.get(1)?.type)
        assertEquals(false, linkClass?.fields?.get(1)?.optional)
        assertEquals(null, linkClass?.fields?.get(1)?.cl)
    }

    @Test
    fun `parser — parse nested object with array`() {
        val obj = JSONObject()
            .put("header", "Header")
            .put("body", "Body text")
            .put("links", JSONArray()
                .put(JSONObject().put("title", "title").put("url", "url"))
                .put(JSONObject().put("title", "title").put("url", "url").put("optional", "true"))
            )
            .put("urls", JSONArray()
                .put(JSONObject().put("title", "title").put("url", "url").put("optional", "true"))
                .put(JSONObject().put("title", "title").put("url", "url"))
            )
            .put("benefits", JSONArray().put("First").put("Second"))
            .put("emptyArray", JSONArray())
            .put("incorrectList", JSONArray()
                .put("String")
                .put(JSONObject().put("title", "Title"))
            )
            .put("incorrectList2", JSONArray()
                .put(JSONObject().put("title", "Title"))
                .put(JSONObject().put("title", JSONObject()))
            )

        val cl = CoppyParser.parseJson(obj, "CoppyContent")

        assertEquals("CoppyContent", cl.name)
        assertEquals(5, cl.fields.size)

        val headerField = cl.fields.get(2)
        assertEquals("header", headerField.key)
        assertEquals("string", headerField.type)
        assertEquals(false, headerField.optional)
        assertEquals(null, headerField.cl)

        val bodyField = cl.fields.get(4)
        assertEquals("body", bodyField.key)
        assertEquals("string", bodyField.type)
        assertEquals(false, bodyField.optional)
        assertEquals(null, bodyField.cl)

        val benefitsField = cl.fields.get(0)
        assertEquals("benefits", benefitsField.key)
        assertEquals("array", benefitsField.type)
        assertEquals(false, benefitsField.optional)
        assertEquals(null, benefitsField.cl)

        val linkField = cl.fields.get(3)
        assertEquals("links", linkField.key)
        assertEquals("array", linkField.type)
        assertEquals(false, linkField.optional)
        val linkClass = linkField.cl
        assertNotNull(linkClass)
        assertEquals("CoppyContentLinks", linkClass?.name)
        assertEquals(3, linkClass?.fields?.size)

        val linkTitleField = linkClass?.fields?.get(0)
        assertEquals("title", linkTitleField?.key)
        assertEquals("string", linkTitleField?.type)
        assertEquals(false, linkTitleField?.optional)
        assertEquals(null, linkTitleField?.cl)

        val linkUrlField = linkClass?.fields?.get(1)
        assertEquals("url", linkUrlField?.key)
        assertEquals("string", linkUrlField?.type)
        assertEquals(false, linkUrlField?.optional)
        assertEquals(null, linkUrlField?.cl)

        val linkOptionalField = linkClass?.fields?.get(2)
        assertEquals("optional", linkOptionalField?.key)
        assertEquals("string", linkOptionalField?.type)
        assertEquals(true, linkOptionalField?.optional)
        assertEquals(null, linkOptionalField?.cl)

        val urlField = cl.fields.get(1)
        assertEquals("urls", urlField.key)
        assertEquals("array", urlField.type)
        assertEquals(false, urlField.optional)
        val urlClass = urlField.cl
        assertNotNull(urlClass)
        assertEquals("CoppyContentUrls", urlClass?.name)
        assertEquals(3, urlClass?.fields?.size)

        val urlTitleField = urlClass?.fields?.get(1)
        assertEquals("title", urlTitleField?.key)
        assertEquals("string", urlTitleField?.type)
        assertEquals(false, urlTitleField?.optional)
        assertEquals(null, urlTitleField?.cl)

        val urlUrlField = urlClass?.fields?.get(2)
        assertEquals("url", urlUrlField?.key)
        assertEquals("string", urlUrlField?.type)
        assertEquals(false, urlUrlField?.optional)
        assertEquals(null, urlUrlField?.cl)

        val urlOptionalField = urlClass?.fields?.get(0)
        assertEquals("optional", urlOptionalField?.key)
        assertEquals("string", urlOptionalField?.type)
        assertEquals(true, urlOptionalField?.optional)
        assertEquals(null, urlOptionalField?.cl)
    }
}