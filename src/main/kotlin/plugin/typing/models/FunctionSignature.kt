package plugin.typing.models

data class FunctionSignatures(
    val functionSignatures: List<FunctionSignature>
)

data class FunctionSignature(
    val returnValueType: String,
    val argumentTypes: Map<String, String>,
    val count: Int
)
