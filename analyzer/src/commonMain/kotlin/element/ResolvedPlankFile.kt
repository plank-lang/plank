package org.plank.analyzer.element

import org.plank.analyzer.BindingViolation
import org.plank.grammar.debug.DontDump
import org.plank.grammar.element.PlankFile
import org.plank.grammar.mapper.SyntaxViolation

/**
 * Represents a [PlankFile] with type definitions. The properties [syntaxViolations],
 * [bindingViolations], [dependencies] will be fulfilled by copy the generated instances
 */
data class ResolvedPlankFile(
  @DontDump val delegate: PlankFile,
  val program: List<ResolvedDecl>,
  @DontDump val syntaxViolations: List<SyntaxViolation> = delegate.violations,
  @DontDump val bindingViolations: List<BindingViolation> = emptyList(),
  @DontDump val dependencies: List<ResolvedPlankFile> = emptyList(),
) : ResolvedPlankElement {
  interface Visitor<T> {
    fun visit(file: ResolvedPlankFile): T = visitPlankFile(file)

    fun visitPlankFile(file: ResolvedPlankFile): T
  }

  val module = delegate.module
  val moduleName = delegate.moduleName
  val path = delegate.path
  val realFile = delegate.realFile

  val isValid = delegate.isValid && bindingViolations.isEmpty()

  override val location = delegate.location
}
