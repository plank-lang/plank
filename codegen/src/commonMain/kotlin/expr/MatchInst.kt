package org.plank.codegen.expr

import org.plank.analyzer.element.TypedIdentPattern
import org.plank.analyzer.element.TypedMatchExpr
import org.plank.analyzer.element.TypedNamedTuplePattern
import org.plank.analyzer.element.TypedPattern
import org.plank.codegen.CodegenContext
import org.plank.codegen.CodegenInstruction
import org.plank.codegen.getField
import org.plank.codegen.unsafeAlloca
import org.plank.llvm4k.ir.IntPredicate
import org.plank.llvm4k.ir.Value

class MatchInst(private val descriptor: TypedMatchExpr) : CodegenInstruction {
  override fun CodegenContext.codegen(): Value {
    val type = descriptor.ty.typegen()
    val patterns = descriptor.patterns.entries.toList()

    val lastPattern = patterns.elementAt(patterns.size - 1)

    val subject = descriptor.subject.codegen()

    val value = patterns
      .reversed()
      .drop(1)
      .foldIndexed(
        fun(): Value = createIf(
          type,
          checkPattern(subject, lastPattern.key, patterns.size - 1),
          { deconstructPattern(subject, lastPattern.key); lastPattern.value.codegen() },
          { createLoad(createAlloca(type)) }
        )
      ) { index, acc, (pattern, expr) ->
        fun(): Value = createIf(
          type,
          checkPattern(subject, pattern, index),
          { deconstructPattern(subject, pattern); expr.codegen() },
          { acc.invoke() }
        )
      }
      .invoke()

    return value
  }
}

fun CodegenContext.checkPattern(
  subject: Value,
  pattern: TypedPattern,
  index: Int, // TODO: remove
): Value {
  return when (pattern) {
    is TypedIdentPattern -> i1.getConstant(1) // true
    is TypedNamedTuplePattern -> {
      val tag = createLoad(getField(subject, 0))

      createICmp(IntPredicate.EQ, tag, i8.getConstant(index))
    }
  }
}

fun CodegenContext.deconstructPattern(subject: Value, pattern: TypedPattern) {
  when (pattern) {
    is TypedIdentPattern -> {
      setSymbol(pattern.name.text, pattern.ty, unsafeAlloca(subject))
    }
    is TypedNamedTuplePattern -> {
      var idx = 1
      val member = createBitCast(subject, pattern.ty.typegen().pointer())

      pattern.properties.forEach { nestedPattern ->
        val prop = getField(member, idx)

        deconstructPattern(prop, nestedPattern)
        idx++
      }
    }
  }
}
