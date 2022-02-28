package org.plank.codegen.expr

import org.plank.analyzer.element.TypedAccessExpr
import org.plank.analyzer.resolver.fullPath
import org.plank.codegen.CodegenContext
import org.plank.codegen.CodegenInstruction
import org.plank.llvm4k.ir.Value

class AccessInst(private val descriptor: TypedAccessExpr) : CodegenInstruction {
  override fun CodegenContext.codegen(): Value {
    val module = findModule(descriptor.scope.fullPath().text) ?: this

    return createLoad(module.getSymbol(descriptor.name.text))
  }
}
