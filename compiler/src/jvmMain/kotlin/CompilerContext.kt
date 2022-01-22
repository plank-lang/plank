package com.gabrielleeg1.plank.compiler

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.left
import arrow.core.traverseEither
import com.gabrielleeg1.plank.analyzer.PlankType
import com.gabrielleeg1.plank.analyzer.element.ResolvedPlankFile
import com.gabrielleeg1.plank.analyzer.element.ResolvedStmt
import com.gabrielleeg1.plank.analyzer.element.TypedExpr
import com.gabrielleeg1.plank.compiler.instructions.CodegenResult
import com.gabrielleeg1.plank.compiler.instructions.CodegenViolation
import com.gabrielleeg1.plank.compiler.instructions.CompilerInstruction
import com.gabrielleeg1.plank.compiler.instructions.element.IRFunction
import com.gabrielleeg1.plank.compiler.instructions.unresolvedVariableError
import com.gabrielleeg1.plank.grammar.element.PlankElement
import org.llvm4j.llvm4j.AllocaInstruction
import org.llvm4j.llvm4j.Context
import org.llvm4j.llvm4j.Function
import org.llvm4j.llvm4j.IRBuilder
import org.llvm4j.llvm4j.Module
import org.llvm4j.llvm4j.NamedStructType
import org.llvm4j.llvm4j.Value

data class CompilerContext(
  val debug: Boolean,
  val module: Module,
  val currentFile: ResolvedPlankFile,
  val name: String = currentFile.module.text,
  val context: Context = module.getContext(),
  val builder: IRBuilder = module.getContext().newIRBuilder(),
  val runtime: PlankRuntime = PlankRuntime(module),
  private val mapper: InstructionMapper = InstructionMapper,
  private val enclosing: CompilerContext? = null,
) {
  private val functions = mutableMapOf<String, IRFunction>()
  private val values = mutableMapOf<String, Pair<PlankType, AllocaInstruction>>()
  private val types = mutableMapOf<String, Pair<PlankType, NamedStructType>>()

  private val expanded = mutableListOf<CompilerContext>()
  private val modules = mutableMapOf<String, CompilerContext>()

  inline fun debug(action: DebugCompilerContext.() -> Unit) {
    if (debug) {
      action(DebugCompilerContext(this))
    }
  }

  fun PlankType.typegen(): TypegenResult {
    return typegen(this)
  }

  fun CompilerInstruction.codegen(): CodegenResult {
    return this@CompilerContext.run { codegen() }
  }

  @JvmName("instructionsCodegen")
  fun Collection<CompilerInstruction>.codegen(): Either<CodegenViolation, List<Value>> {
    return traverseEither { it.codegen() }
  }

  @JvmName("elementsCodegen")
  fun Collection<PlankElement>.codegen(): Either<CodegenViolation, List<Value>> {
    return traverseEither { it.codegen() }
  }

  fun PlankElement.codegen(): CodegenResult {
    return when (this) {
      is TypedExpr -> mapper.visit(this).codegen()
      is ResolvedStmt -> mapper.visit(this).codegen()
      else -> TODO("Not implemented mapping for ${this::class.simpleName}")
    }
  }

  fun createFileScope(file: ResolvedPlankFile = currentFile): CompilerContext = copy(
    enclosing = this,
    currentFile = file,
    name = file.module.text,
  )

  inline fun createNestedScope(
    moduleName: String,
    builder: CompilerContext.() -> Unit = {},
  ): CompilerContext = copy(enclosing = this, name = moduleName).apply(builder)

  inline operator fun invoke(builder: CompilerContext.() -> Unit): CompilerContext {
    return apply(builder)
  }

  fun addFunction(function: IRFunction): Either<CodegenViolation, Function> {
    functions[function.name] = function

    return with(function) {
      this@CompilerContext.codegen()
    }
  }

  fun addStruct(name: String, type: PlankType, struct: NamedStructType) {
    types[name] = type to struct
  }

  fun addVariable(name: String, type: PlankType, variable: AllocaInstruction) {
    values[name] = type to variable
  }

  fun expand(module: CompilerContext) {
    expanded += module
  }

  fun addModule(module: CompilerContext) {
    modules[module.name] = module
  }

  fun findModule(name: String): CompilerContext? {
    return modules[name]
      ?: enclosing?.findModule(name)
      ?: expanded.filter { it != this }.firstNotNullOfOrNull { it.findModule(name) }
  }

  fun findFunction(name: String): IRFunction? {
    return functions[name]
      ?: enclosing?.findFunction(name)
      ?: expanded.filter { it != this }.firstNotNullOfOrNull { it.findFunction(name) }
  }

  fun findStruct(name: String): NamedStructType? {
    return types[name]?.second
      ?: enclosing?.findStruct(name)
      ?: expanded.filter { it != this }.firstNotNullOfOrNull { it.findStruct(name) }
  }

  fun findVariable(name: String): Either<CodegenViolation, AllocaInstruction> = either.eager {
    findVariableAlloca(name)
      ?: findFunction(name)?.accessIn(this@CompilerContext)
      ?: unresolvedVariableError(name).left().bind<AllocaInstruction>()
  }

  fun findVariableAlloca(name: String): AllocaInstruction? {
    return values[name]?.second
      ?: enclosing?.findVariableAlloca(name)
      ?: expanded.filter { it != this }.firstNotNullOfOrNull { it.findVariableAlloca(name) }
  }

  override fun toString(): String {
    return "PlankContext($name, $enclosing)"
  }
}
