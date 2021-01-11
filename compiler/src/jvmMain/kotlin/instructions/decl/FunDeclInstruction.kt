package com.lorenzoog.jplank.compiler.instructions.decl

import com.lorenzoog.jplank.compiler.PlankContext
import com.lorenzoog.jplank.compiler.instructions.PlankInstruction
import com.lorenzoog.jplank.compiler.utils.FunctionUtils
import com.lorenzoog.jplank.element.Decl
import com.lorenzoog.jplank.element.Stmt
import com.lorenzoog.jplank.element.visit
import io.vexelabs.bitbuilder.llvm.ir.Value
import io.vexelabs.bitbuilder.llvm.ir.values.FunctionValue
import io.vexelabs.bitbuilder.llvm.support.VerifierFailureAction

class FunDeclInstruction(private val descriptor: Decl.FunDecl) : PlankInstruction() {
  override fun codegen(context: PlankContext): Value? {
    val returnType = context.binding.visit(descriptor.returnType)
    val function = descriptor.genFunction(context) as? FunctionValue? ?: return null

    context.createScope().also { functionContext ->
      val body = function.createBlock("entry")
      functionContext.builder.setPositionAtEnd(body)

      function.getParameters().forEachIndexed { index, parameter ->
        val type = parameter.getType()

        val entry = descriptor.realParameters.entries.toList().getOrElse(index) {
          return context.report("function parameter with index $index is not defined", descriptor)
        }

        val name = entry.key.text
          ?: return context.report("parameter with index $index name is null", descriptor)

        val variable = functionContext.builder.createAlloca(type, name)
        functionContext.builder.createStore(parameter, variable)
        functionContext.addVariable(name, variable)
      }

      functionContext.map(descriptor.body).map {
        it.codegen(functionContext)
      }

      if (returnType.isVoid && descriptor.body.filterIsInstance<Stmt.ReturnStmt>().isEmpty()) {
        context.builder.createRetVoid()
      }

      if (!function.verify(VerifierFailureAction.PrintMessage)) {
        return context.report("invalid function", descriptor)
      }
    }

    return function
  }

  companion object {
    fun Decl.FunDecl.genFunction(context: PlankContext): Value? {
      val name = name.text ?: return context.report("missing function name", this)

      val parameters = parameters
        .map {
          context.map(context.binding.visit(it))
            ?: return context.report("failed to handle argument", it)
        }

      val returnType = context.map(context.binding.visit(returnType))
        ?: return context.report("return type is null", this)

      val type = context.llvm.getFunctionType(
        returns = returnType,
        *parameters.toTypedArray(),
        variadic = false
      )

      return context.module.createFunction(FunctionUtils.generateName(name, context), type)
    }
  }
}
