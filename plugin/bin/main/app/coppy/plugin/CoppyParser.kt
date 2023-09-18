package app.coppy.plugin

import org.json.JSONArray
import org.json.JSONObject

@Suppress("unused")
internal object CoppyParser {
    fun parseJson(obj: JSONObject, name: String): Class {
        val fields: MutableList<Field> = arrayListOf()

        for (key in obj.keys()) {
            val field = getField(key, obj.get(key), name)
            if (field != null) fields.add(field)
        }

        return Class(fields, name)
    }

    private fun getField(key: String, value: Any, parentName: String): Field? {
        if (value is String) {
            return Field(key, "string", value == "", null)
        }
        if (value is JSONObject) {
            return Field(key, "object", false, parseJson(value, parentName + key.capitalize()))
        }
        if (value is JSONArray) {
            var valid = true
            var type: String? = null
            var cl: Class? = null

            for (item in value) {
                when (type) {
                    null -> {
                        if (item is String) type = "string"
                        if (item is JSONObject) {
                            type = "object"
                            cl = parseJson(item, parentName + key.capitalize())
                        }
                    }
                    "string" -> {
                        if (item !is String) valid = false
                    }
                    "object" -> {
                        if (item !is JSONObject || cl == null) valid = false
                        else {
                            var fields = cl.fields.map { f -> f.key } + item.keySet()
                            val fieldsSet = fields.toSet()

                            for (fieldName in fieldsSet) {
                                var field = cl.fields.find { f -> f.key == fieldName}

                                if (field != null) {
                                    if (!item.has(fieldName)) {
                                        field.optional = true
                                    } else {
                                        val itemField = item.get(fieldName)
                                        if (
                                            (itemField is String && field.type != "string") ||
                                            (itemField is JSONObject && field.type != "object") ||
                                            (itemField is JSONArray && field.type != "array")
                                        ) {
                                            valid = false
                                        }
                                    }
                                } else {
                                    field = getField(fieldName, item.get(fieldName), cl.name)
                                    if (field != null) {
                                        field.optional = true
                                        cl.fields.add(field)
                                    }
                                }

                            }
                        }
                    }
                }
            }

            if (valid && type != null) {
                return Field(key, "array", false, cl)
            }
        }

        return null
    }
}
