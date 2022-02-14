package org.plank.analyzer.infer

import org.plank.analyzer.element.TypedExpr
import org.plank.analyzer.element.TypedIntAddExpr
import org.plank.analyzer.element.TypedIntDivExpr
import org.plank.analyzer.element.TypedIntEQExpr
import org.plank.analyzer.element.TypedIntGTEExpr
import org.plank.analyzer.element.TypedIntGTExpr
import org.plank.analyzer.element.TypedIntLTEExpr
import org.plank.analyzer.element.TypedIntLTExpr
import org.plank.analyzer.element.TypedIntMulExpr
import org.plank.analyzer.element.TypedIntNEQExpr
import org.plank.analyzer.element.TypedIntSubExpr
import org.plank.syntax.element.Identifier
import org.plank.syntax.element.PlankFile

data class Variable(
  val mutable: Boolean,
  val name: Identifier,
  val ty: Ty,
  val declaredIn: Scope,
  val isInScope: Boolean = false,
) {
  override fun toString(): String {
    return "Variable(mutable=$mutable, name=$name, ty=$ty, isInScope=$isInScope)"
  }
}

class GlobalScope(override val moduleTree: ModuleTree) : Scope() {
  /**
   * Init compiler-defined functions
   */
  init {
    create(charTy)
    create(boolTy)
    create(i32Ty)
    create(doubleTy)
    create(floatTy)

    // Add default binary operators
    inlineFun("+", i32Ty, i32Ty, i32Ty) { (a, b) -> TypedIntAddExpr(a, b) }
    inlineFun("-", i32Ty, i32Ty, i32Ty) { (a, b) -> TypedIntSubExpr(a, b) }
    inlineFun("*", i32Ty, i32Ty, i32Ty) { (a, b) -> TypedIntMulExpr(a, b) }
    inlineFun("/", i32Ty, i32Ty, i32Ty) { (a, b) -> TypedIntDivExpr(a, b) }

    // Add default logical operators
    inlineFun("==", boolTy, i32Ty, i32Ty) { (a, b) -> TypedIntEQExpr(a, b) }
    inlineFun("!=", boolTy, i32Ty, i32Ty) { (a, b) -> TypedIntNEQExpr(a, b) }
    inlineFun(">=", boolTy, i32Ty, i32Ty) { (a, b) -> TypedIntGTEExpr(a, b) }
    inlineFun(">", boolTy, i32Ty, i32Ty) { (a, b) -> TypedIntGTExpr(a, b) }
    inlineFun("<=", boolTy, i32Ty, i32Ty) { (a, b) -> TypedIntLTEExpr(a, b) }
    inlineFun("<", boolTy, i32Ty, i32Ty) { (a, b) -> TypedIntLTExpr(a, b) }
  }

  override val name = Identifier("Global")
  override val enclosing: Scope? = null

  private fun inlineFun(
    name: String,
    returnType: Ty,
    vararg parameters: Ty,
    builder: (List<TypedExpr>) -> TypedExpr,
  ) {
//    declare(
//      Identifier(name),
//      FunctionType(
//        returnType,
//        parameters.toList(),
//        parameters.withIndex().associate { Identifier(it.index.toString()) to it.value }
//      ).copy(isInline = true, actualReturnType = returnType, inlineCall = {
//        ResolvedExprBody(builder(it))
//      })
//    )
  }
}

data class FileScope(
  val file: PlankFile,
  override val enclosing: Scope? = null,
  override val moduleTree: ModuleTree = ModuleTree(),
) : Scope() {
  override val name = file.module
  override val nested = false
}

data class ModuleScope(
  val module: Module,
  override val enclosing: Scope,
  override val moduleTree: ModuleTree = ModuleTree()
) : Scope() {
  override val name: Identifier = Identifier("${enclosing.name}.${module.name}")
}

class FunctionScope(
  val function: FunctionInfo,
  override val name: Identifier,
  override val enclosing: Scope? = null,
  override val moduleTree: ModuleTree = ModuleTree(),
  override val references: MutableMap<Identifier, Ty> = LinkedHashMap(),
) : Scope() {
  override val isTopLevelScope: Boolean = false
}

open class ClosureScope(
  override val name: Identifier,
  override val enclosing: Scope,
  override val references: MutableMap<Identifier, Ty> = LinkedHashMap(),
  override val moduleTree: ModuleTree = ModuleTree()
) : Scope() {
  override val isTopLevelScope: Boolean = false
}

sealed class Scope {
  abstract val name: Identifier
  abstract val enclosing: Scope?
  abstract val moduleTree: ModuleTree
  open val isTopLevelScope: Boolean = true
  open val nested: Boolean get() = enclosing != null

  val variables = mutableMapOf<Identifier, Variable>()
  open val references = mutableMapOf<Identifier, Ty>()

  private val types = mutableMapOf<Identifier, Ty>()
  private val expanded = mutableListOf<Scope>()

  fun expand(another: Scope): Scope {
    expanded += another

    return this
  }

  /**
   * Declares a compiler-defined variable with type [type] in the context
   */
  fun declare(name: Identifier, type: Ty, mutable: Boolean = false) {
    variables[name] = Variable(mutable, name, type, this)
  }

  fun declare(name: Identifier, value: TypedExpr, mutable: Boolean = false) {
    variables[name] = Variable(mutable, name, value.type, this)
  }

  fun create(type: Ty) {
    when (type) {
      is AppTy -> error("Can not create a type from an application")
      is ConstTy -> types[Identifier(type.name)] = type
      is VarTy -> types[Identifier(type.name)] = type
    }
  }

  fun create(name: Identifier, type: Ty) {
    types[name] = type
  }

  fun findModule(name: Identifier): Module? {
    return moduleTree.findModule(name)
      ?: enclosing?.findModule(name)
  }

  fun findType(name: Identifier): Ty? {
    return types[name]
      ?: enclosing?.findType(name)
      ?: expanded.filter { it != this }.firstNotNullOfOrNull { it.findType(name) }
  }

  fun findVariable(name: Identifier): Variable? {
    return variables[name]?.copy(isInScope = true)
      ?: enclosing?.findVariable(name)?.copy(isInScope = false)
      ?: expanded.filter { it != this }.firstNotNullOfOrNull { it.findVariable(name) }
        ?.copy(isInScope = false)
  }
}
