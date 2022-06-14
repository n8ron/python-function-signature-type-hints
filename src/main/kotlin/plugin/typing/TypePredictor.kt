package plugin.typing

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.jetbrains.python.psi.PyFunction
import plugin.typing.models.FunctionSignature

class TypePredictor {

    companion object {
        private const val PATH_TO_JSON_FILE = "data/FunctionSignatures.json"
        private val GSON = GsonBuilder().setPrettyPrinting().serializeNulls().create()


        fun predictParameters(function: PyFunction): Map<String, List<String>> {
            val reader = JsonReader(
                this::class.java.classLoader.getResourceAsStream(PATH_TO_JSON_FILE).bufferedReader()
            )
            val functionName = function.qualifiedName
            val jsonObject = JsonParser.parseReader(reader).asJsonObject
            val jsonArray = jsonObject[functionName].asJsonArray
            val paramNameToTypes = mutableMapOf<String, MutableList<String>>()
            for (functionSignature in GSON.fromJson(jsonArray, Array<FunctionSignature>::class.java)) {
                functionSignature.argumentTypes.forEach {
                    (name, type) -> paramNameToTypes.getOrPut(name){mutableListOf()}.add(type)
                }
            }
            return paramNameToTypes
        }

        fun predictReturnType(function: PyFunction): List<String> {
            val reader = JsonReader(
                this::class.java.classLoader.getResourceAsStream(PATH_TO_JSON_FILE).bufferedReader()
            )
            val functionName = function.qualifiedName
            val jsonObject = JsonParser.parseReader(reader).asJsonObject
            println(jsonObject)
            return jsonObject[functionName]?.let { functionSignatures ->
                GSON.fromJson(functionSignatures.asJsonArray, Array<FunctionSignature>::class.java).map {
                    it.returnValueType
                }
            } ?: emptyList()
        }
    }

}
