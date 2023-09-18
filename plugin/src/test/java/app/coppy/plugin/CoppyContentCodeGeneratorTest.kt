package app.coppy.plugin

import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CoppyContentCodeGeneratorTest {
    @Test
    fun `object generator — generate simple object`() {
        val cl = Class(
            arrayListOf(
                Field("header", "string", false, null),
                Field("cta", "string", true, null),
                Field("body", "string", false, null),
            ),
            "TestContent"
        )

        val generatedClass = CoppyContentCodeGenerator.generateClassesForObject(cl)

        val result = """
            @Suppress("unused")
            class TestContent(
                private var _header: String,
                private var _cta: String?,
                private var _body: String,
            ): Serializable {
                val header get() = _header
                val cta get() = _cta
                val body get() = _body
            
                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _header = obj.optString("header", _header)
                    _cta = obj.tryString("cta")
                    _body = obj.optString("body", _body)
                }
            
                companion object {
                    internal fun createFrom(obj: JSONObject?): TestContent? {
                        if (obj == null) return null
                        val tempHeader = obj.tryString("header")
                        val tempCta = obj.tryString("cta")
                        val tempBody = obj.tryString("body")
                        if (tempHeader == null || tempBody == null) return null
                        return TestContent(tempHeader, tempCta, tempBody)
                    }
                }
            }
        """.trimIndent()

        assertEquals(result, generatedClass)
    }

    @Test
    fun `object generator — with object props`() {
        val bodyClass = Class(arrayListOf(
            Field("body", "string", false, null),
            Field("title", "string", false, null),
        ), "TestContentBody")
        val linkClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("url", "string", false, null),
        ), "TestContentLink")
        val cl = Class(
            arrayListOf(
                Field("body", "object", false, bodyClass),
                Field("link", "object", true, linkClass),
                Field("title", "string", false, null),
            ),
            "TestContent"
        )

        val generatedClass = CoppyContentCodeGenerator.generateClassesForObject(cl)

        val result = """
            @Suppress("unused")
            class TestContentBody(
                private var _body: String,
                private var _title: String,
            ): Serializable {
                val body get() = _body
                val title get() = _title

                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _body = obj.optString("body", _body)
                    _title = obj.optString("title", _title)
                }

                companion object {
                    internal fun createFrom(obj: JSONObject?): TestContentBody? {
                        if (obj == null) return null
                        val tempBody = obj.tryString("body")
                        val tempTitle = obj.tryString("title")
                        if (tempBody == null || tempTitle == null) return null
                        return TestContentBody(tempBody, tempTitle)
                    }
                }
            }
            @Suppress("unused")
            class TestContentLink(
                private var _title: String,
                private var _url: String,
            ): Serializable {
                val title get() = _title
                val url get() = _url

                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _title = obj.optString("title", _title)
                    _url = obj.optString("url", _url)
                }

                companion object {
                    internal fun createFrom(obj: JSONObject?): TestContentLink? {
                        if (obj == null) return null
                        val tempTitle = obj.tryString("title")
                        val tempUrl = obj.tryString("url")
                        if (tempTitle == null || tempUrl == null) return null
                        return TestContentLink(tempTitle, tempUrl)
                    }
                }
            }
            @Suppress("unused")
            class TestContent(
                val body: TestContentBody,
                private var _link: TestContentLink?,
                private var _title: String,
            ): Serializable {
                val link get() = _link
                val title get() = _title
            
                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    body.update(obj.optJSONObject("body"))
                    _link = TestContentLink.createFrom(obj.optJSONObject("link"))
                    _title = obj.optString("title", _title)
                }
            
                companion object {
                    internal fun createFrom(obj: JSONObject?): TestContent? {
                        if (obj == null) return null
                        val tempBody = TestContentBody.createFrom(obj.optJSONObject("body"))
                        val tempLink = TestContentLink.createFrom(obj.optJSONObject("link"))
                        val tempTitle = obj.tryString("title")
                        if (tempBody == null || tempTitle == null) return null
                        return TestContent(tempBody, tempLink, tempTitle)
                    }
                }
            }
        """.trimIndent()

        assertEquals(result, generatedClass)
    }

    @Test
    fun `object generator — with array props`() {
        val questionClass = Class(arrayListOf(
            Field("answer", "string", false, null),
            Field("question", "string", false, null),
        ), "TestContentQuestion")
        val linkClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("url", "string", false, null),
        ), "TestContentLink")
        val cl = Class(
            arrayListOf(
                Field("questions", "array", false, questionClass),
                Field("links", "array", true, linkClass),
                Field("benefits", "array", false, null),
                Field("title", "string", false, null),
            ),
            "TestContent"
        )

        val generatedClass = CoppyContentCodeGenerator.generateClassesForObject(cl)

        val result = """
            @Suppress("unused")
            class TestContentQuestion(
                private var _answer: String,
                private var _question: String,
            ): Serializable {
                val answer get() = _answer
                val question get() = _question

                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _answer = obj.optString("answer", _answer)
                    _question = obj.optString("question", _question)
                }

                companion object {
                    internal fun createFrom(obj: JSONObject?): TestContentQuestion? {
                        if (obj == null) return null
                        val tempAnswer = obj.tryString("answer")
                        val tempQuestion = obj.tryString("question")
                        if (tempAnswer == null || tempQuestion == null) return null
                        return TestContentQuestion(tempAnswer, tempQuestion)
                    }
                }
            }
            @Suppress("unused")
            class TestContentLink(
                private var _title: String,
                private var _url: String,
            ): Serializable {
                val title get() = _title
                val url get() = _url

                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _title = obj.optString("title", _title)
                    _url = obj.optString("url", _url)
                }

                companion object {
                    internal fun createFrom(obj: JSONObject?): TestContentLink? {
                        if (obj == null) return null
                        val tempTitle = obj.tryString("title")
                        val tempUrl = obj.tryString("url")
                        if (tempTitle == null || tempUrl == null) return null
                        return TestContentLink(tempTitle, tempUrl)
                    }
                }
            }
            @Suppress("unused")
            class TestContent(
                private var _questions: List<TestContentQuestion>,
                private var _links: List<TestContentLink>?,
                private var _benefits: List<String>,
                private var _title: String,
            ): Serializable {
                val questions get() = _questions
                val links get() = _links
                val benefits get() = _benefits
                val title get() = _title
            
                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    val newQuestions = obj.optJSONArray("questions")
                    if (newQuestions != null) {
                        val questionsList: MutableList<TestContentQuestion> = arrayListOf()
                        for (i in 0 until newQuestions.length()) {
                            val tempQuestions = TestContentQuestion.createFrom(newQuestions.optJSONObject(i))
                            if (tempQuestions != null) questionsList.add(tempQuestions)
                        }
                        _questions = questionsList.toList()
                    }
                    val newLinks = obj.optJSONArray("links")
                    if (newLinks != null) {
                        val linksList: MutableList<TestContentLink> = arrayListOf()
                        for (i in 0 until newLinks.length()) {
                            val tempLinks = TestContentLink.createFrom(newLinks.optJSONObject(i))
                            if (tempLinks != null) linksList.add(tempLinks)
                        }
                        _links = linksList.toList()
                    } else {
                        _links = null
                    }
                    val newBenefits = obj.optJSONArray("benefits")
                    if (newBenefits != null) {
                        val benefitsList: MutableList<String> = arrayListOf()
                        for (i in 0 until newBenefits.length()) {
                            val tempBenefits = newBenefits.tryString(i)
                            if (tempBenefits != null) benefitsList.add(tempBenefits)
                        }
                        _benefits = benefitsList.toList()
                    }
                    _title = obj.optString("title", _title)
                }
            
                companion object {
                    private fun createQuestionsList(newQuestions: JSONArray?): List<TestContentQuestion>? {
                        if (newQuestions == null) return null
                        val questionsList: MutableList<TestContentQuestion> = arrayListOf()
                        for (i in 0 until newQuestions.length()) {
                            val tempQuestions = TestContentQuestion.createFrom(newQuestions.optJSONObject(i))
                            if (tempQuestions != null) questionsList.add(tempQuestions)
                        }
                        return questionsList.toList()
                    }
                    private fun createLinksList(newLinks: JSONArray?): List<TestContentLink>? {
                        if (newLinks == null) return null
                        val linksList: MutableList<TestContentLink> = arrayListOf()
                        for (i in 0 until newLinks.length()) {
                            val tempLinks = TestContentLink.createFrom(newLinks.optJSONObject(i))
                            if (tempLinks != null) linksList.add(tempLinks)
                        }
                        return linksList.toList()
                    }
                    private fun createBenefitsList(newBenefits: JSONArray?): List<String>? {
                        if (newBenefits == null) return null
                        val benefitsList: MutableList<String> = arrayListOf()
                        for (i in 0 until newBenefits.length()) {
                            val tempBenefits = newBenefits.tryString(i)
                            if (tempBenefits != null) benefitsList.add(tempBenefits)
                        }
                        return benefitsList.toList()
                    }
                    internal fun createFrom(obj: JSONObject?): TestContent? {
                        if (obj == null) return null
                        val tempQuestions = createQuestionsList(obj.optJSONArray("questions"))
                        val tempLinks = createLinksList(obj.optJSONArray("links"))
                        val tempBenefits = createBenefitsList(obj.optJSONArray("benefits"))
                        val tempTitle = obj.tryString("title")
                        if (tempQuestions == null || tempBenefits == null || tempTitle == null) return null
                        return TestContent(tempQuestions, tempLinks, tempBenefits, tempTitle)
                    }
                }
            }
        """.trimIndent()

        assertEquals(result, generatedClass)
    }

    @Test
    fun `content class generator — flat content`() {
        val content = JSONObject()
            .put("title", "Title")
            .put("body", JSONObject().put("header", "Header").put("body", "Body text"))
            .put("benefits", JSONArray().put("First").put("Second").put("Third"))

        val bodyClass = Class(arrayListOf(
            Field("header", "string", false, null),
            Field("body", "string", false, null),
            Field("optional", "string", true, null),
        ), "CoppyContentBody")

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, bodyClass),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        val generatedClass = CoppyContentCodeGenerator.generateContentClass(contentClass, content)

        val result = """
            @Suppress("unused")
            class CoppyContentBody(
                private var _header: String,
                private var _body: String,
                private var _optional: String?,
            ): Serializable {
                val header get() = _header
                val body get() = _body
                val optional get() = _optional
            
                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _header = obj.optString("header", _header)
                    _body = obj.optString("body", _body)
                    _optional = obj.tryString("optional")
                }
            
                companion object {
                    internal fun createFrom(obj: JSONObject?): CoppyContentBody? {
                        if (obj == null) return null
                        val tempHeader = obj.tryString("header")
                        val tempBody = obj.tryString("body")
                        val tempOptional = obj.tryString("optional")
                        if (tempHeader == null || tempBody == null) return null
                        return CoppyContentBody(tempHeader, tempBody, tempOptional)
                    }
                }
            }
            @Suppress("unused")
            class CoppyContent: Serializable, Updatable {
                private var _title: String = "Title"
                val title get() = _title
                val body: CoppyContentBody = CoppyContentBody(
                    "Header",
                    "Body text",
                    null,
                )
                private var _benefits: List<String> = arrayListOf(
                    "First",
                    "Second",
                    "Third",
                )
                val benefits get() = _benefits
            
                override fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _title = obj.optString("title", _title)
                    body.update(obj.optJSONObject("body"))
                    val newBenefits = obj.optJSONArray("benefits")
                    if (newBenefits != null) {
                        val benefitsList: MutableList<String> = arrayListOf()
                        for (i in 0 until newBenefits.length()) {
                            val tempBenefits = newBenefits.tryString(i)
                            if (tempBenefits != null) benefitsList.add(tempBenefits)
                        }
                        _benefits = benefitsList.toList()
                    }
                }
            }
            """.trimIndent()

        assertEquals(result, generatedClass)
    }
    @Test
    fun `content class generator — nested content`() {
        val content = JSONObject()
            .put("title", "Title")
            .put("body", JSONObject().put("header", "Header").put("body", "Body text").put("nested", JSONObject().put("header", "Nested header")))
            .put("benefits", JSONArray().put("First").put("Second").put("Third"))

        val nestedClass = Class(arrayListOf(
            Field("header", "string", false, null),
        ), "CoppyContentBodyNested")

        val bodyClass = Class(arrayListOf(
            Field("header", "string", false, null),
            Field("body", "string", false, null),
            Field("nested", "object", false, nestedClass),
        ), "CoppyContentBody")

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, bodyClass),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        val generatedClass = CoppyContentCodeGenerator.generateContentClass(contentClass, content)

        val result = """
            @Suppress("unused")
            class CoppyContentBodyNested(
                private var _header: String,
            ): Serializable {
                val header get() = _header
            
                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _header = obj.optString("header", _header)
                }
            
                companion object {
                    internal fun createFrom(obj: JSONObject?): CoppyContentBodyNested? {
                        if (obj == null) return null
                        val tempHeader = obj.tryString("header")
                        if (tempHeader == null) return null
                        return CoppyContentBodyNested(tempHeader)
                    }
                }
            }
            @Suppress("unused")
            class CoppyContentBody(
                private var _header: String,
                private var _body: String,
                val nested: CoppyContentBodyNested,
            ): Serializable {
                val header get() = _header
                val body get() = _body
            
                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _header = obj.optString("header", _header)
                    _body = obj.optString("body", _body)
                    nested.update(obj.optJSONObject("nested"))
                }
            
                companion object {
                    internal fun createFrom(obj: JSONObject?): CoppyContentBody? {
                        if (obj == null) return null
                        val tempHeader = obj.tryString("header")
                        val tempBody = obj.tryString("body")
                        val tempNested = CoppyContentBodyNested.createFrom(obj.optJSONObject("nested"))
                        if (tempHeader == null || tempBody == null || tempNested == null) return null
                        return CoppyContentBody(tempHeader, tempBody, tempNested)
                    }
                }
            }
            @Suppress("unused")
            class CoppyContent: Serializable, Updatable {
                private var _title: String = "Title"
                val title get() = _title
                val body: CoppyContentBody = CoppyContentBody(
                    "Header",
                    "Body text",
                    CoppyContentBodyNested(
                        "Nested header",
                    ),
                )
                private var _benefits: List<String> = arrayListOf(
                    "First",
                    "Second",
                    "Third",
                )
                val benefits get() = _benefits
            
                override fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _title = obj.optString("title", _title)
                    body.update(obj.optJSONObject("body"))
                    val newBenefits = obj.optJSONArray("benefits")
                    if (newBenefits != null) {
                        val benefitsList: MutableList<String> = arrayListOf()
                        for (i in 0 until newBenefits.length()) {
                            val tempBenefits = newBenefits.tryString(i)
                            if (tempBenefits != null) benefitsList.add(tempBenefits)
                        }
                        _benefits = benefitsList.toList()
                    }
                }
            }
            """.trimIndent()

        assertEquals(result, generatedClass)
    }
    @Test
    fun `content class generator — content with missing optional object value`() {
        val content = JSONObject()
            .put("title", "Title")
            .put("body", JSONObject().put("header", "Header").put("body", "Body text"))
            .put("benefits", JSONArray().put("First").put("Second").put("Third"))

        val optionalClass = Class(arrayListOf(
            Field("header", "string", false, null),
        ), "CoppyContentBodyOptional")

        val bodyClass = Class(arrayListOf(
            Field("header", "string", false, null),
            Field("body", "string", false, null),
            Field("optional", "object", true, optionalClass),
        ), "CoppyContentBody")

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, bodyClass),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        val generatedClass = CoppyContentCodeGenerator.generateContentClass(contentClass, content)

        val result = """
            @Suppress("unused")
            class CoppyContentBodyOptional(
                private var _header: String,
            ): Serializable {
                val header get() = _header
            
                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _header = obj.optString("header", _header)
                }
            
                companion object {
                    internal fun createFrom(obj: JSONObject?): CoppyContentBodyOptional? {
                        if (obj == null) return null
                        val tempHeader = obj.tryString("header")
                        if (tempHeader == null) return null
                        return CoppyContentBodyOptional(tempHeader)
                    }
                }
            }
            @Suppress("unused")
            class CoppyContentBody(
                private var _header: String,
                private var _body: String,
                private var _optional: CoppyContentBodyOptional?,
            ): Serializable {
                val header get() = _header
                val body get() = _body
                val optional get() = _optional
            
                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _header = obj.optString("header", _header)
                    _body = obj.optString("body", _body)
                    _optional = CoppyContentBodyOptional.createFrom(obj.optJSONObject("optional"))
                }
            
                companion object {
                    internal fun createFrom(obj: JSONObject?): CoppyContentBody? {
                        if (obj == null) return null
                        val tempHeader = obj.tryString("header")
                        val tempBody = obj.tryString("body")
                        val tempOptional = CoppyContentBodyOptional.createFrom(obj.optJSONObject("optional"))
                        if (tempHeader == null || tempBody == null) return null
                        return CoppyContentBody(tempHeader, tempBody, tempOptional)
                    }
                }
            }
            @Suppress("unused")
            class CoppyContent: Serializable, Updatable {
                private var _title: String = "Title"
                val title get() = _title
                val body: CoppyContentBody = CoppyContentBody(
                    "Header",
                    "Body text",
                    null,
                )
                private var _benefits: List<String> = arrayListOf(
                    "First",
                    "Second",
                    "Third",
                )
                val benefits get() = _benefits
            
                override fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _title = obj.optString("title", _title)
                    body.update(obj.optJSONObject("body"))
                    val newBenefits = obj.optJSONArray("benefits")
                    if (newBenefits != null) {
                        val benefitsList: MutableList<String> = arrayListOf()
                        for (i in 0 until newBenefits.length()) {
                            val tempBenefits = newBenefits.tryString(i)
                            if (tempBenefits != null) benefitsList.add(tempBenefits)
                        }
                        _benefits = benefitsList.toList()
                    }
                }
            }
            """.trimIndent()

        assertEquals(result, generatedClass)
    }

    @Test
    fun `content class generator — content with missing optional string value`() {
        val content = JSONObject()
            .put("title", "Title")
            .put("body", JSONObject().put("header", "Header").put("body", "Body text").put("optional", ""))
            .put("benefits", JSONArray().put("First").put("Second").put("Third"))

        val bodyClass = Class(arrayListOf(
            Field("header", "string", false, null),
            Field("body", "string", false, null),
            Field("optional", "string", true, null),
        ), "CoppyContentBody")

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, bodyClass),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        val generatedClass = CoppyContentCodeGenerator.generateContentClass(contentClass, content)

        val result = """
            @Suppress("unused")
            class CoppyContentBody(
                private var _header: String,
                private var _body: String,
                private var _optional: String?,
            ): Serializable {
                val header get() = _header
                val body get() = _body
                val optional get() = _optional
            
                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _header = obj.optString("header", _header)
                    _body = obj.optString("body", _body)
                    _optional = obj.tryString("optional")
                }
            
                companion object {
                    internal fun createFrom(obj: JSONObject?): CoppyContentBody? {
                        if (obj == null) return null
                        val tempHeader = obj.tryString("header")
                        val tempBody = obj.tryString("body")
                        val tempOptional = obj.tryString("optional")
                        if (tempHeader == null || tempBody == null) return null
                        return CoppyContentBody(tempHeader, tempBody, tempOptional)
                    }
                }
            }
            @Suppress("unused")
            class CoppyContent: Serializable, Updatable {
                private var _title: String = "Title"
                val title get() = _title
                val body: CoppyContentBody = CoppyContentBody(
                    "Header",
                    "Body text",
                    null,
                )
                private var _benefits: List<String> = arrayListOf(
                    "First",
                    "Second",
                    "Third",
                )
                val benefits get() = _benefits
            
                override fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _title = obj.optString("title", _title)
                    body.update(obj.optJSONObject("body"))
                    val newBenefits = obj.optJSONArray("benefits")
                    if (newBenefits != null) {
                        val benefitsList: MutableList<String> = arrayListOf()
                        for (i in 0 until newBenefits.length()) {
                            val tempBenefits = newBenefits.tryString(i)
                            if (tempBenefits != null) benefitsList.add(tempBenefits)
                        }
                        _benefits = benefitsList.toList()
                    }
                }
            }
            """.trimIndent()

        assertEquals(result, generatedClass)
    }
    @Test
    fun `content class generator — content with missing optional array value`() {
        val content = JSONObject()
            .put("title", "Title")
            .put("body", JSONObject().put("header", "Header").put("body", "Body text"))
            .put("benefits", JSONArray().put("First").put("Second").put("Third"))

        val optionalClass = Class(arrayListOf(
            Field("header", "string", false, null),
        ), "CoppyContentBodyOptional")

        val bodyClass = Class(arrayListOf(
            Field("header", "string", false, null),
            Field("body", "string", false, null),
            Field("optional", "array", true, optionalClass),
        ), "CoppyContentBody")

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, bodyClass),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        val generatedClass = CoppyContentCodeGenerator.generateContentClass(contentClass, content)

        val result = """
            @Suppress("unused")
            class CoppyContentBodyOptional(
                private var _header: String,
            ): Serializable {
                val header get() = _header
            
                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _header = obj.optString("header", _header)
                }
            
                companion object {
                    internal fun createFrom(obj: JSONObject?): CoppyContentBodyOptional? {
                        if (obj == null) return null
                        val tempHeader = obj.tryString("header")
                        if (tempHeader == null) return null
                        return CoppyContentBodyOptional(tempHeader)
                    }
                }
            }
            @Suppress("unused")
            class CoppyContentBody(
                private var _header: String,
                private var _body: String,
                private var _optional: List<CoppyContentBodyOptional>?,
            ): Serializable {
                val header get() = _header
                val body get() = _body
                val optional get() = _optional
            
                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _header = obj.optString("header", _header)
                    _body = obj.optString("body", _body)
                    val newOptional = obj.optJSONArray("optional")
                    if (newOptional != null) {
                        val optionalList: MutableList<CoppyContentBodyOptional> = arrayListOf()
                        for (i in 0 until newOptional.length()) {
                            val tempOptional = CoppyContentBodyOptional.createFrom(newOptional.optJSONObject(i))
                            if (tempOptional != null) optionalList.add(tempOptional)
                        }
                        _optional = optionalList.toList()
                    } else {
                        _optional = null
                    }
                }
            
                companion object {
                    private fun createOptionalList(newOptional: JSONArray?): List<CoppyContentBodyOptional>? {
                        if (newOptional == null) return null
                        val optionalList: MutableList<CoppyContentBodyOptional> = arrayListOf()
                        for (i in 0 until newOptional.length()) {
                            val tempOptional = CoppyContentBodyOptional.createFrom(newOptional.optJSONObject(i))
                            if (tempOptional != null) optionalList.add(tempOptional)
                        }
                        return optionalList.toList()
                    }
                    internal fun createFrom(obj: JSONObject?): CoppyContentBody? {
                        if (obj == null) return null
                        val tempHeader = obj.tryString("header")
                        val tempBody = obj.tryString("body")
                        val tempOptional = createOptionalList(obj.optJSONArray("optional"))
                        if (tempHeader == null || tempBody == null) return null
                        return CoppyContentBody(tempHeader, tempBody, tempOptional)
                    }
                }
            }
            @Suppress("unused")
            class CoppyContent: Serializable, Updatable {
                private var _title: String = "Title"
                val title get() = _title
                val body: CoppyContentBody = CoppyContentBody(
                    "Header",
                    "Body text",
                    null,
                )
                private var _benefits: List<String> = arrayListOf(
                    "First",
                    "Second",
                    "Third",
                )
                val benefits get() = _benefits
            
                override fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _title = obj.optString("title", _title)
                    body.update(obj.optJSONObject("body"))
                    val newBenefits = obj.optJSONArray("benefits")
                    if (newBenefits != null) {
                        val benefitsList: MutableList<String> = arrayListOf()
                        for (i in 0 until newBenefits.length()) {
                            val tempBenefits = newBenefits.tryString(i)
                            if (tempBenefits != null) benefitsList.add(tempBenefits)
                        }
                        _benefits = benefitsList.toList()
                    }
                }
            }
            """.trimIndent()

        assertEquals(result, generatedClass)
    }
    @Test
    fun `content class generator — throws if required string value is missing`() {
        val content = JSONObject()
            .put("title", "Title")
            .put("body", JSONObject().put("header", "Header").put("body", "Body text"))
            .put("benefits", JSONArray().put("First").put("Second").put("Third"))

        val bodyClass = Class(arrayListOf(
            Field("header", "string", false, null),
            Field("body", "string", false, null),
            Field("required", "string", false, null),
        ), "CoppyContentBody")

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, bodyClass),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        assertThrows(
            Throwable::class.java,
            fun () {
                CoppyContentCodeGenerator.generateContentClass(contentClass, content)
            },
            "Cannot generate code. Missing required string value: CoppyContentBody.required"
        )
    }
    @Test
    fun `content class generator — throws if required object value is missing`() {
        val content = JSONObject()
            .put("title", "Title")
            .put("body", JSONObject().put("header", "Header").put("body", "Body text"))
            .put("benefits", JSONArray().put("First").put("Second").put("Third"))

        val requiredClass = Class(arrayListOf(
            Field("header", "string", false, null),
        ), "CoppyContentBodyRequired")

        val bodyClass = Class(arrayListOf(
            Field("header", "string", false, null),
            Field("body", "string", false, null),
            Field("required", "object", false, requiredClass),
        ), "CoppyContentBody")

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, bodyClass),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        assertThrows(
            Throwable::class.java,
            fun () {
                CoppyContentCodeGenerator.generateContentClass(contentClass, content)
            },
            "Cannot generate code. Missing required object value: CoppyContentBody.required"
        )
    }
    @Test
    fun `content class generator — throws if required object class is missing`() {
        val content = JSONObject()
            .put("title", "Title")
            .put("body", JSONObject().put("header", "Header").put("body", "Body text").put("required", JSONObject()))
            .put("benefits", JSONArray().put("First").put("Second").put("Third"))

        val bodyClass = Class(arrayListOf(
            Field("header", "string", false, null),
            Field("body", "string", false, null),
            Field("required", "object", false, null),
        ), "CoppyContentBody")

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, bodyClass),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        assertThrows(
            Throwable::class.java,
            fun () {
                CoppyContentCodeGenerator.generateContentClass(contentClass, content)
            },
            "Cannot generate code. Missing class description for object property: CoppyContentBody.required"
        )
    }
    @Test
    fun `content class generator — throws if required array value is missing`() {
        val content = JSONObject()
            .put("title", "Title")
            .put("body", JSONObject().put("header", "Header").put("body", "Body text"))
            .put("benefits", JSONArray().put("First").put("Second").put("Third"))


        val bodyClass = Class(arrayListOf(
            Field("header", "string", false, null),
            Field("body", "string", false, null),
            Field("required", "array", false, null),
        ), "CoppyContentBody")

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, bodyClass),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        assertThrows(
            Throwable::class.java,
            fun () {
                CoppyContentCodeGenerator.generateContentClass(contentClass, content)
            },
            "Cannot generate code. Missing required array value: CoppyContentBody.required",
        )
    }
    @Test
    fun `content class generator — throws if required string value is missing on core class`() {
        val content = JSONObject()
            .put("body", JSONObject().put("header", "Header").put("body", "Body text"))
            .put("benefits", JSONArray().put("First").put("Second").put("Third"))

        val bodyClass = Class(arrayListOf(
            Field("header", "string", false, null),
            Field("body", "string", false, null),
        ), "CoppyContentBody")

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, bodyClass),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        assertThrows(
            Throwable::class.java,
            fun () {
                CoppyContentCodeGenerator.generateContentClass(contentClass, content)
            },
            "Cannot generate code. Missing required string value in core content class: title",
        )
    }
    @Test
    fun `content class generator — throws if required object value is missing on core class`() {
        val content = JSONObject()
            .put("title", "Title")
            .put("benefits", JSONArray().put("First").put("Second").put("Third"))

        val requiredClass = Class(arrayListOf(
            Field("header", "string", false, null),
        ), "CoppyContentBodyRequired")

        val bodyClass = Class(arrayListOf(
            Field("header", "string", false, null),
            Field("body", "string", false, null),
            Field("required", "object", false, requiredClass),
        ), "CoppyContentBody")

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, bodyClass),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        assertThrows(
            Throwable::class.java,
            fun () {
                CoppyContentCodeGenerator.generateContentClass(contentClass, content)
            },
            "Cannot generate code. Missing required object value in core content class: body",
        )
    }
    @Test
    fun `content class generator — throws if required object class is missing on core class`() {
        val content = JSONObject()
            .put("title", "Title")
            .put("body", JSONObject().put("header", "Header").put("body", "Body text").put("required", JSONObject()))
            .put("benefits", JSONArray().put("First").put("Second").put("Third"))

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, null),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        assertThrows(
            Throwable::class.java,
            fun () {
                CoppyContentCodeGenerator.generateContentClass(contentClass, content)
            },
            "Cannot generate code. Missing class description for class property in core class: body"
        )
    }
    @Test
    fun `content class generator — throws if required array value is missing on core class`() {
        val content = JSONObject()
            .put("title", "Title")
            .put("body", JSONObject().put("header", "Header").put("body", "Body text"))


        val bodyClass = Class(arrayListOf(
            Field("header", "string", false, null),
            Field("body", "string", false, null),
            Field("required", "array", false, null),
        ), "CoppyContentBody")

        val contentClass = Class(arrayListOf(
            Field("title", "string", false, null),
            Field("body", "object", false, bodyClass),
            Field("benefits", "array", false, null)
        ), "CoppyContent")

        assertThrows(
            Throwable::class.java,
            fun () {
                CoppyContentCodeGenerator.generateContentClass(contentClass, content)
            },
            "Cannot generate code. Missing required array value in core content class: benefits"
        )
    }

    @Test
    fun `content file generator — generates file content`() {
        val obj = JSONObject()
            .put("header", "Header")
            .put("body", "Body text")
            .put("links", JSONArray()
                .put(JSONObject().put("title", "title").put("url", "url"))
                .put(JSONObject().put("title", "title").put("url", "url").put("optional", "true"))
            )
            .put("benefits", JSONArray().put("First").put("Second"))
            .put("emptyArray", JSONArray())
            .put("incorrectList", JSONArray()
                .put("String")
                .put(JSONObject().put("title", "Title"))
            )

        val generated = CoppyContentCodeGenerator.generateContentFileContent(obj)

        val result="""
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
            class CoppyContentLinks(
                private var _title: String,
                private var _url: String,
                private var _optional: String?,
            ): Serializable {
                val title get() = _title
                val url get() = _url
                val optional get() = _optional

                internal fun update(obj: JSONObject?) {
                    if (obj == null) return
                    _title = obj.optString("title", _title)
                    _url = obj.optString("url", _url)
                    _optional = obj.tryString("optional")
                }

                companion object {
                    internal fun createFrom(obj: JSONObject?): CoppyContentLinks? {
                        if (obj == null) return null
                        val tempTitle = obj.tryString("title")
                        val tempUrl = obj.tryString("url")
                        val tempOptional = obj.tryString("optional")
                        if (tempTitle == null || tempUrl == null) return null
                        return CoppyContentLinks(tempTitle, tempUrl, tempOptional)
                    }
                }
            }
            @Suppress("unused")
            class CoppyContent: Serializable, Updatable {
                private var _benefits: List<String> = arrayListOf(
                    "First",
                    "Second",
                )
                val benefits get() = _benefits
                private var _header: String = "Header"
                val header get() = _header
                private var _links: List<CoppyContentLinks> = arrayListOf(
                    CoppyContentLinks(
                        "title",
                        "url",
                        null,
                    ),
                    CoppyContentLinks(
                        "title",
                        "url",
                        "true",
                    ),
                )
                val links get() = _links
                private var _body: String = "Body text"
                val body get() = _body

                override fun update(obj: JSONObject?) {
                    if (obj == null) return
                    val newBenefits = obj.optJSONArray("benefits")
                    if (newBenefits != null) {
                        val benefitsList: MutableList<String> = arrayListOf()
                        for (i in 0 until newBenefits.length()) {
                            val tempBenefits = newBenefits.tryString(i)
                            if (tempBenefits != null) benefitsList.add(tempBenefits)
                        }
                        _benefits = benefitsList.toList()
                    }
                    _header = obj.optString("header", _header)
                    val newLinks = obj.optJSONArray("links")
                    if (newLinks != null) {
                        val linksList: MutableList<CoppyContentLinks> = arrayListOf()
                        for (i in 0 until newLinks.length()) {
                            val tempLinks = CoppyContentLinks.createFrom(newLinks.optJSONObject(i))
                            if (tempLinks != null) linksList.add(tempLinks)
                        }
                        _links = linksList.toList()
                    }
                    _body = obj.optString("body", _body)
                }
            }
        """.trimIndent()

        assertEquals(result, generated)
    }
}

