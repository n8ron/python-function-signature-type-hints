package plugin.typing

import com.intellij.openapi.util.Ref
import com.jetbrains.python.psi.PyCallable
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyNamedParameter
import com.jetbrains.python.psi.types.PyType
import com.jetbrains.python.psi.types.PyTypeParser
import com.jetbrains.python.psi.types.PyTypeProviderBase
import com.jetbrains.python.psi.types.PyUnionType
import com.jetbrains.python.psi.types.TypeEvalContext

class PyGuessTypeProvider : PyTypeProviderBase() {

    override fun getParameterType(param: PyNamedParameter, func: PyFunction, context: TypeEvalContext): Ref<PyType>? {
        if (shouldNotInfer(param)) {
            return super.getParameterType(param, func, context)
        }
        val paramName = param.name
        val typesName = TypePredictor.predictParameters(func)[paramName]
        // TODO NumpyUfuncs.isUFunc NumpyDocStringTypeProvider
        return typesName?.let {
             Ref.create(
                PyUnionType.union(
                    typesName.map {
                        PyTypeParser.getTypeByName(
                            func,
                            it,
                            context
                        )
                    }
                )
            )
        }
    }

    private fun shouldNotInfer(param: PyNamedParameter): Boolean {
        return param.isPositionalContainer || param.isKeywordContainer || param.isKeywordOnly || param.isSelf
    }

    override fun getReturnType(callable: PyCallable, context: TypeEvalContext): Ref<PyType>? {
        if (callable !is PyFunction) {
            return super.getReturnType(callable, context)
        }
        val typesName = TypePredictor.predictReturnType(callable)
        return Ref.create(
            PyUnionType.union(
                typesName.map {
                    PyTypeParser.getTypeByName(
                        callable,
                        it,
                        context
                    )
                }
            )
        )
    }
}
