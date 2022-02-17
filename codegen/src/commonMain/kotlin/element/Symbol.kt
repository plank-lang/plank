package org.plank.codegen.element

import org.plank.analyzer.infer.Ty
import org.plank.codegen.CodegenContext
import org.plank.codegen.CodegenInstruction
import org.plank.codegen.alloca
import org.plank.codegen.expr.createIf
import org.plank.codegen.getField
import org.plank.codegen.mangle
import org.plank.llvm4k.ir.AddrSpace
import org.plank.llvm4k.ir.Function
import org.plank.llvm4k.ir.FunctionType
import org.plank.llvm4k.ir.User
import org.plank.llvm4k.ir.Value
import org.plank.syntax.element.Identifier

sealed interface Symbol : CodegenInstruction {
  val ty: Ty

  fun CodegenContext.access(): User?
}

class ValueSymbol(override val ty: Ty, private val value: User) : Symbol {
  override fun CodegenContext.access(): User = value

  override fun CodegenContext.codegen(): Value = value
}

class LazySymbol(
  override val ty: Ty,
  val name: String,
  val lazyValue: CodegenContext.() -> Value,
) : Symbol {
  private var getter: Function? = null

  override fun CodegenContext.access(): User? {
    val getter = getter ?: return null

    return lazyLocal(name) {
      alloca(createCall(getter, name = "lazy.call.$name"), "lazy.$name")
    }
  }

  override fun CodegenContext.codegen(): Value {
    val type = ty.typegen()
    val name = mangle(name)

    val struct = createNamedStruct(name) {
      elements = listOf(type.pointer(AddrSpace.Generic))
    }

    val variable = currentModule.addGlobalVariable(name, struct, AddrSpace.Generic).apply {
      initializer = struct.getConstant(
        type.pointer(AddrSpace.Generic).constPointerNull(),
        isPacked = false
      )
    }

    val insertionBlock = insertionBlock

    getter = currentModule
      .addFunction(mangle(Identifier(this@LazySymbol.name), Identifier("Get")), FunctionType(type))
      .apply {
        positionAfter(createBasicBlock("entry").also(::appendBasicBlock))

        val field = getField(variable, 0)

        createIf(
          type, createIsNull(createLoad(field)),
          { createStore(alloca(lazyValue()), field) },
        )

        createRet(createLoad(createLoad(getField(variable, 0))))
      }

    if (insertionBlock != null) {
      positionAfter(insertionBlock)
    }

    return variable
  }
}
