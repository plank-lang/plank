package com.gabrielleeg1.plank.compiler.instructions.expr

import arrow.core.computations.either
import com.gabrielleeg1.plank.analyzer.element.TypedSizeofExpr
import com.gabrielleeg1.plank.compiler.CompilerContext
import com.gabrielleeg1.plank.compiler.getSize
import com.gabrielleeg1.plank.compiler.instructions.CodegenResult
import com.gabrielleeg1.plank.compiler.instructions.CompilerInstruction

class SizeofInstruction(private val descriptor: TypedSizeofExpr) : CompilerInstruction {
  override fun CompilerContext.codegen(): CodegenResult = either.eager {
    descriptor.type.typegen().bind().getSize()
  }
}
