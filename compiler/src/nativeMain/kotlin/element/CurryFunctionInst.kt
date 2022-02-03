package org.plank.compiler.element

import arrow.core.identity
import org.plank.analyzer.FunctionType
import org.plank.analyzer.PlankType
import org.plank.analyzer.element.ResolvedFunDecl
import org.plank.compiler.CodegenContext
import org.plank.compiler.ExecContext
import org.plank.compiler.alloca
import org.plank.compiler.castClosure
import org.plank.compiler.createScopeContext
import org.plank.compiler.mangleFunction
import org.plank.grammar.element.Identifier
import org.plank.llvm4k.ir.AllocaInst
import org.plank.llvm4k.ir.Value

class CurryFunctionInst(
  private val type: FunctionType,
  private val nested: Boolean,
  private val references: Map<Identifier, PlankType>,
  override val name: String,
  private val mangled: String,
  private val returnType: PlankType,
  private val realParameters: Map<Identifier, PlankType>,
  private val generate: GenerateBody,
) : FunctionInst {
  private val parameters = realParameters.entries.toList().map { it.toPair() }

  override fun CodegenContext.access(): AllocaInst? {
    return lazyLocal("curry.$name") {
      currentModule.getFunction(mangled)?.let {
        alloca(createCall(it), "curry.$name") // get instance of curried function
      }
    }
  }

  override fun CodegenContext.codegen(): Value {
    val reversedParameters = realParameters.keys
    val closure: Value

    createScopeContext(name) {
      closure = if (parameters.isNotEmpty()) {
        List(parameters.size - 1, ::identity)
          .reversed()
          .fold(generateNesting(reversedParameters.size - 1)) { acc, i ->
            generateNesting(i) { returnType ->
              val func = acc.also { it.codegen() }.access()!!
              val type = returnType.unsafeCast<FunctionType>().typegen()

              createRet(castClosure(func, type))
            }
          }
          .also { it.codegen() }
          .access()!!
      } else {
        addIrClosure(name, type, references, generate)
          .also { it.codegen() }
          .access()!!
      }
    }

    if (nested) {
      setSymbol(name, type, closure as AllocaInst)
    }

    return closure
  }

  private fun generateNesting(
    index: Int,
    builder: ExecContext.(returnType: PlankType) -> Unit = { generate() }
  ): ClosureFunctionInst {
    val type = FunctionType(
      parameters[index].second,
      when (val returnType = type.nest(index)) {
        is FunctionType -> returnType.copy(isNested = true)
        else -> returnType
      }
    )

    return ClosureFunctionInst(
      name = "$mangled#$index",
      mangled = "$mangled{{closure}}#$index",
      type = type.copy(name = Identifier("$mangled#$index")),
      references = references + parameters,
      parameters = mapOf(parameters[index]),
      generate = { builder(type.returnType) },
    )
  }
}

fun CodegenContext.addCurryFunction(
  descriptor: ResolvedFunDecl,
  nested: Boolean = false,
  generate: GenerateBody,
): Value = addFunction(
  CurryFunctionInst(
    type = descriptor.type,
    nested = nested,
    references = descriptor.references,
    name = descriptor.name.text,
    mangled = mangleFunction(descriptor),
    returnType = descriptor.type.actualReturnType,
    realParameters = descriptor.realParameters,
    generate = generate,
  )
)
