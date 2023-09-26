package app.coppy.plugin

import org.apache.commons.text.StringEscapeUtils
import org.json.JSONArray
import org.json.JSONObject

@Suppress("unused")
internal object CoppyContentCodeGenerator {
    val fileHeader = """
       |package app.coppy.generatedCoppy
       |
       |import org.json.JSONObject
       |import org.json.JSONArray
       |import java.io.Serializable
       |import app.coppy.Updatable
       |
       |internal fun JSONObject.tryString(key: String): String? {
       |    val temp = this.optString(key)
       |    if (temp == "") return null
       |    return temp
       |}
       |internal fun JSONArray.tryString(key: Int): String? {
       |    val temp = this.optString(key)
       |    if (temp == "") return null
       |    return temp
       |}
    """.trimMargin()

    fun generateContentFileContent(content: JSONObject): String {
        val cl = CoppyParser.parseJson(content, "CoppyContent")

        return """${fileHeader}
            |${generateContentClass(cl, content)}
            """.trimMargin()
    }

    fun generateClassesForObject(cl: Class): String {
        val nestedClasses: MutableList<String> = arrayListOf()
        var variables = ""
        var properties = ""

        for (field in cl.fields) {
            when (field.type) {
                "string" -> {
                    variables += """
                   |    private var _${field.key}: String${if (field.optional) "?" else ""},"""
                    properties += """
                   |    val ${field.key} get() = _${field.key}"""
                }
                "object" -> {
                    if (field.cl == null) continue
                    nestedClasses.add(generateClassesForObject(field.cl))
                    if (field.optional) {
                        variables += """
                       |    private var _${field.key}: ${field.cl.name}?,"""
                        properties += """
                       |    val ${field.key} get() = _${field.key}"""
                    } else {
                        variables += """
                       |    val ${field.key}: ${field.cl.name},"""
                    }
                }
                "array" -> {
                    val className = if (field.cl != null) field.cl.name else "String"
                    if (field.cl != null) {
                        nestedClasses.add(generateClassesForObject(field.cl))
                    }
                    variables += """
                   |    private var _${field.key}: List<${className}>${if (field.optional) "?" else ""},"""
                    properties += """
                   |    val ${field.key} get() = _${field.key}"""
                }
            }
        }

        return """${nestedClasses.joinToString("\n")}
            |@Suppress("unused")
            |class ${cl.name}($variables
            |): Serializable {$properties
            |${generateUpdaterForClass(cl)}
            |${generateCreatorForClass(cl)}
            |}
        """.trimMargin()
    }

    private fun generateUpdaterForClass(cl: Class, isPublic: Boolean = false): String {
        val updaterValueName = "obj"
        var updaters = ""

        for (field in cl.fields) {
            when (field.type) {
                "string" -> {
                    if (field.optional) {
                        updaters += """
                       |        _${field.key} = ${updaterValueName}.tryString("${field.key}")"""
                    } else {
                        updaters += """
                       |        _${field.key} = ${updaterValueName}.optString("${field.key}", _${field.key})"""
                    }

                }
                "object" -> {
                    if (field.cl == null) continue
                    if (field.optional) {
                        updaters += """
                       |        _${field.key} = ${field.cl.name}.createFrom(${updaterValueName}.optJSONObject("${field.key}"))"""
                    } else {
                        updaters += """
                       |        ${field.key}.update(${updaterValueName}.optJSONObject("${field.key}"))"""
                    }

                }
                "array" -> {
                    val className = if (field.cl != null) field.cl.name else "String"
                    updaters += """
                   |        val new${field.key.capitalize()} = obj.optJSONArray("${field.key}")
                   |        if (new${field.key.capitalize()} != null) {
                   |            val ${field.key}List: MutableList<${className}> = arrayListOf()
                   |            for (i in 0 until new${field.key.capitalize()}.length()) {
                   |                val temp${field.key.capitalize()} = ${if (className == "String") "new${field.key.capitalize()}.tryString(i)" else "${className}.createFrom(new${field.key.capitalize()}.optJSONObject(i))"}
                   |                if (temp${field.key.capitalize()} != null) ${field.key}List.add(temp${field.key.capitalize()})
                   |            }
                   |            _${field.key} = ${field.key}List.toList()
                   |        }"""
                    if (field.optional) {
                        updaters += """ else {
                       |            _${field.key} = null
                       |        }"""
                    }
                }
            }
        }

        return """
       |    ${if (isPublic) "override" else "internal"} fun update(${updaterValueName}: JSONObject?) {
       |        if (${updaterValueName} == null) return$updaters
       |    }"""
    }

    private fun generateCreatorForClass(cl: Class): String {
        var values = ""
        val check: MutableList<String> = arrayListOf()
        var listCreators = ""

        for (field in cl.fields) {
            when (field.type) {
                "string" -> {
                    values += """
                   |            val temp${field.key.capitalize()} = obj.tryString("${field.key}")"""
                    if (!field.optional) check.add("temp${field.key.capitalize()} == null")
                }
                "object" -> {
                    if (field.cl == null) continue
                    values += """
                   |            val temp${field.key.capitalize()} = ${field.cl.name}.createFrom(obj.optJSONObject("${field.key}"))"""
                    if (!field.optional) check.add("temp${field.key.capitalize()} == null")
                }
                "array" -> {
                    val className = if (field.cl != null) field.cl.name else "String"
                    values += """
                   |            val temp${field.key.capitalize()} = create${field.key.capitalize()}List(obj.optJSONArray("${field.key}"))"""
                    listCreators += """
                   |        private fun create${field.key.capitalize()}List(new${field.key.capitalize()}: JSONArray?): List<${className}>? {
                   |            if (new${field.key.capitalize()} == null) return null
                   |            val ${field.key}List: MutableList<${className}> = arrayListOf()
                   |            for (i in 0 until new${field.key.capitalize()}.length()) {
                   |                val temp${field.key.capitalize()} = ${if (className == "String") "new${field.key.capitalize()}.tryString(i)" else "${className}.createFrom(new${field.key.capitalize()}.optJSONObject(i))"}
                   |                if (temp${field.key.capitalize()} != null) ${field.key}List.add(temp${field.key.capitalize()})
                   |            }
                   |            return ${field.key}List.toList()
                   |        }"""
                    if (!field.optional) check.add("temp${field.key.capitalize()} == null")
                }
            }
        }

        return """
       |    companion object {$listCreators
       |        internal fun createFrom(obj: JSONObject?): ${cl.name}? {
       |            if (obj == null) return null${values}
       |            if (${check.joinToString(" || ")}) return null
       |            return ${cl.name}(${cl.fields.map {f -> "temp${f.key.capitalize()}"}.joinToString(", ")})
       |        }
       |    }"""
    }

    private fun getListValue(cl: Class?, values: JSONArray, depth: Int = 0): String {
        val indent = "    ".repeat(depth + 1)
        var vals = ""

        if (cl == null) {
            // Array of strings
            for (value in values) {
                if (value is String) {
                    vals += """
                   |${indent}"${StringEscapeUtils.escapeJava(value)}","""
                }
            }
        } else {
            for (value in values) {
                if (value is JSONObject) {
                    vals += """
                   |${indent}${getClassValue(cl, value, depth + 1)},"""
                }
            }
        }

        return """arrayListOf($vals
       |${"    ".repeat(depth)})"""
    }

    private fun getClassValue(cl: Class, values: JSONObject, depth: Int = 0): String {
        val indent = "    ".repeat(depth + 1)
        var vals = ""

        for (field in cl.fields) {
            when (field.type) {
                "string" -> {
                    val str = values.optString(field.key)
                    if (str == "" && !field.optional) {
                        throw Exception("Cannot generate code. Missing required string value: ${cl.name}.${field.key}")
                    }
                    vals += """
                   |${indent}${if (str == "") "null" else """"${StringEscapeUtils.escapeJava(str)}""""},"""
                }
                "object" -> {
                    val obj = values.optJSONObject(field.key)
                    if (field.cl == null) {
                        throw Exception("Cannot generate code. Missing class description for object property: ${cl.name}.${field.key}")
                    }
                    if (obj == null && !field.optional) {
                        throw Exception("Cannot generate code. Missing required object value: ${cl.name}.${field.key}")
                    }
                    vals += """
                   |${indent}${if (obj == null) "null" else getClassValue(field.cl, obj, depth + 1)},"""
                }
                "array" -> {
                    val arr = values.optJSONArray(field.key)
                    if (arr == null && !field.optional) {
                        throw Exception("Cannot generate code. Missing required array value: ${cl.name}.${field.key}")
                    }
                    vals += """
                   |${indent}${if (arr == null) "null" else getListValue(field.cl, arr, depth  +1)},"""
                }
            }
        }

        return """${cl.name}($vals
       |${"    ".repeat(depth)})"""
    }
    fun generateContentClass(cl: Class, content: JSONObject): String {
        val nestedClasses: MutableList<String> = arrayListOf()
        var variables = ""

        for (field in cl.fields) {
            when (field.type) {
                "string" -> {
                    val str = content.optString(field.key)
                    if (str == "") {
                        throw Exception("Cannot generate code. Missing required string value in core content class: ${field.key}")
                    }
                    variables += """
                   |    private var _${field.key}: String = "${StringEscapeUtils.escapeJava(str)}"
                   |    val ${field.key} get() = _${field.key}"""
                }
                "object" -> {
                    val obj = content.optJSONObject(field.key)
                    if (obj == null) {
                        throw Exception("Cannot generate code. Missing required object value in core content class: ${field.key}")
                    }
                    if (field.cl == null) {
                        throw Exception("Cannot generate code. Missing class description for class property in core class: ${field.key}")
                    }
                    nestedClasses.add(generateClassesForObject(field.cl))
                    variables += """
                   |    val ${field.key}: ${field.cl.name} = ${getClassValue(field.cl, obj, 1)}"""
                }
                "array" -> {
                    val arr = content.optJSONArray(field.key)
                    if (arr == null)  {
                        throw Exception("Cannot generate code. Missing required array value in core content class: ${field.key}")
                    }
                    if (field.cl != null) {
                        nestedClasses.add(generateClassesForObject(field.cl))
                    }
                    variables += """
                   |    private var _${field.key}: List<${if (field.cl == null) "String" else field.cl.name}> = ${getListValue(field.cl, arr, 1)}
                   |    val ${field.key} get() = _${field.key}"""
                }
            }
        }

        return """${nestedClasses.joinToString("\n")}
       |@Suppress("unused")
       |class CoppyContent: Serializable, Updatable {$variables
       |${generateUpdaterForClass(cl, true)}
       |}""".trimMargin()
    }
}

internal class Field(
    val key: String,
    val type: String,
    var optional: Boolean,
    val cl: Class?,
) {}

internal class Class(
    val fields: MutableList<Field>,
    val name: String
)

internal fun String.capitalize(): String {
    return this.replaceFirstChar { it.uppercaseChar() } }