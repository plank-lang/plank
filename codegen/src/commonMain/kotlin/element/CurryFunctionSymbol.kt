package org.plank.codegen.element

import arrow.core.identity
import org.plank.analyzer.element.ResolvedFunDecl
import org.plank.analyzer.infer.FunTy
import org.plank.analyzer.infer.Subst
import org.plank.analyzer.infer.Ty
import org.plank.codegen.alloca
import org.plank.codegen.castClosure
import org.plank.codegen.mangle
import org.plank.codegen.scope.CodegenCtx
import org.plank.codegen.scope.ExecContext
import org.plank.codegen.scope.createScopeContext
import org.plank.llvm4k.ir.AllocaInst
import org.plank.llvm4k.ir.Value
import org.plank.syntax.element.Identifier

class CurryFunctionSymbol(
  override val ty: FunTy,
  override val name: String,
  private val mangled: String,
  private val nested: Boolean,
  private val references: Map<Identifier, Ty>,
  private val realParameters: Map<Identifier, Ty>,
  private val generate: GenerateBody,
) : FunctionSymbol {
  private val parameters = realParameters.entries.toList().map { it.toPair() }

  override fun CodegenCtx.access(subst: Subst): AllocaInst? {
    return currentModule.getFunction(mangled)?.let {
      alloca(createCall(it), "curry.$name") // get instance of curried function
    }
  }

  override fun CodegenCtx.codegen(): Value {
    val reversedParameters = realParameters.keys
    val closure: Value

    createScopeContext(name) {
      closure = if (parameters.isNotEmpty()) {
        List(parameters.size - 1, ::identity)
          .reversed()
          .fold(nested(reversedParameters.size - 1)) { acc, i ->
            nested(i) { returnTy ->
              val func = acc.also { it.codegen() }.access()!!
              val ty = returnTy.typegen()

              createRet(castClosure(func, ty))
            }
          }
          .also { it.codegen() }
          .access()!!
      } else {
        addClosure(name, ty.returnTy, "${mangled}_empty", references, generate = generate)
          .also { it.codegen() }
          .access()!!
      }
    }

    if (nested) {
      setSymbol(name, ty, closure as AllocaInst)
    }

    return closure
  }

  private fun nested(index: Int, builder: NestBuilder = { generate() }): ClosureFunctionSymbol {
    val ty = FunTy(parameters[index].second, ty.nest(index))

    return ClosureFunctionSymbol(
      name = "$mangled#$index",
      mangled = "$mangled{{closure}}#$index",
      ty = ty,
      returnTy = ty.returnTy,
      references = references + parameters.dropLast(1),
      parameters = mapOf(parameters[index]),
      realParameters = realParameters,
      generate = { builder(ty.returnTy) },
    )
  }
}

typealias NestBuilder = ExecContext.(returnType: Ty) -> Unit

fun CodegenCtx.addCurryFunction(
  descriptor: ResolvedFunDecl,
  nested: Boolean = false,
  generate: GenerateBody,
): Value = addFunction(
  CurryFunctionSymbol(
    ty = descriptor.ty,
    nested = nested,
    references = descriptor.references,
    name = descriptor.name.text,
    mangled = mangle(descriptor),
    realParameters = descriptor.parameters,
    generate = generate,
  )
)
