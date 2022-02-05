package org.plank.codegen

import org.plank.llvm4k.ir.Function
import org.plank.llvm4k.ir.FunctionType
import org.plank.llvm4k.ir.Linkage
import org.plank.llvm4k.ir.Value
import org.plank.syntax.message.lineSeparator

class DebugContext(private val context: CodegenContext) {
  private val printf: Function by lazy {
    context.currentModule.getFunction("printf")
      ?: FunctionType(context.void, context.i8.pointer(), isVarargs = true)
        .let { context.currentModule.addFunction("printf", it) }
        .apply {
          linkage = Linkage.External
        }
  }

  fun println(message: String, vararg args: Value) {
    val debug by context.createGlobalStringPtr("$message$lineSeparator")

    context.createCall(printf, debug, *args)
  }
}
