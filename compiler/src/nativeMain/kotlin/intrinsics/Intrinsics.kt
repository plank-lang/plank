package com.gabrielleeg1.plank.compiler.intrinsics

import com.gabrielleeg1.plank.compiler.CodegenContext
import com.gabrielleeg1.plank.compiler.ExecContext
import com.gabrielleeg1.plank.compiler.createUnit
import com.gabrielleeg1.plank.grammar.message.lineSeparator
import org.plank.llvm4k.Context
import org.plank.llvm4k.ir.Argument
import org.plank.llvm4k.ir.Function
import org.plank.llvm4k.ir.FunctionType
import org.plank.llvm4k.ir.Linkage
import org.plank.llvm4k.ir.Type
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

class Intrinsics(builder: Intrinsics.() -> Unit = {}) {
  private val modules: MutableMap<String, IntrinsicModule.() -> Unit> = linkedMapOf()

  init {
    builder()
  }

  fun module(name: String, builder: IntrinsicModule.() -> Unit) {
    modules[name] = builder
  }

  fun toFunctionMap(context: CodegenContext): Map<String, IntrinsicFunction> {
    return modules
      .flatMap { (name, builder) ->
        val module = IntrinsicModule(name, context).apply(builder)

        module.toFunctionMap().map { Triple(module, it.key, it.value) }
      }
      .associate { (module, name, function) -> "${module.name}.$name" to function }
  }

  operator fun Intrinsics.unaryPlus() {
    this@Intrinsics.modules += this.modules
  }
}

class IntrinsicModule(val name: String, val context: CodegenContext) : Context by context {
  private val functions: MutableMap<String, IntrinsicFunction> = linkedMapOf()

  val module by context::currentModule
  val unit by context::unit

  fun function(
    returnType: Type,
    vararg parameters: Type,
    varargs: Boolean = false,
    builder: IntrinsicFunction.() -> Unit = {},
  ) = PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Function>> { _, property ->
    val type = FunctionType(returnType, *parameters, isVarargs = varargs)

    functions[property.name] = IntrinsicFunction().apply(builder)

    ReadOnlyProperty { _, _ ->
      context.currentModule.getFunction(property.name)
        ?: context.currentModule.addFunction(property.name, type)
    }
  }

  fun toFunctionMap(): Map<String, IntrinsicFunction> = functions
}

class IntrinsicFunction {
  private var function: (Function.() -> Unit)? = null
  private var entry: (ExecContext.() -> Unit)? = null

  fun function(builder: Function.() -> Unit) {
    function = builder
  }

  fun entry(builder: ExecContext.(List<Argument>) -> Unit) {
    entry = {
      builder(function.arguments.drop(1))
    }
  }

  fun build(context: ExecContext) {
    function?.invoke(context.function)
    entry?.invoke(context)
  }
}

@Suppress("UNUSED_VARIABLE", "Detekt.UnusedPrivateMember")
val DefaultIntrinsics = Intrinsics {
  module("Std.IO") {
    val printf by function(i32, i8.pointer(), varargs = true) {
      function {
        linkage = Linkage.External
      }
    }

    val println by function(unit, i8.pointer()) {
      entry { (msg) ->
        createCall(printf, createGlobalStringPtr("%s$lineSeparator", "str"), msg)
        createRet(createUnit())
      }
    }

    val print by function(unit, i8.pointer()) {
      entry { (msg) ->
        createCall(printf, createGlobalStringPtr("%s", "str"), msg)
        createRet(createUnit())
      }
    }
  }
}
