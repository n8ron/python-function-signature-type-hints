package plugin.typing

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.PyUnionType
import plugin.typing.models.FunctionSignature
import java.io.FileReader

class TypePredictor {



    companion object {
        private const val PATH_TO_JSON_FILE = "FunctionSignatures.json"
        private val GSON = GsonBuilder().setPrettyPrinting().serializeNulls().create()
        private val reader = JsonReader(FileReader(PATH_TO_JSON_FILE))

        fun predictParameters(function: PyFunction): Map<String, List<String>> {
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
            val functionName = function.qualifiedName
            val jsonObject = JsonParser.parseReader(reader).asJsonObject
            val jsonArray = jsonObject[functionName].asJsonArray
            return GSON.fromJson(jsonArray, Array<FunctionSignature>::class.java).map {
                it.returnValueType
            }
        }
    }

}
