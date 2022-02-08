package org.plank.analyzer.phases

import org.plank.analyzer.element.ResolvedCodeBody
import org.plank.analyzer.element.ResolvedEnumDecl
import org.plank.analyzer.element.ResolvedErrorDecl
import org.plank.analyzer.element.ResolvedErrorStmt
import org.plank.analyzer.element.ResolvedExprBody
import org.plank.analyzer.element.ResolvedExprStmt
import org.plank.analyzer.element.ResolvedFunDecl
import org.plank.analyzer.element.ResolvedFunctionBody
import org.plank.analyzer.element.ResolvedLetDecl
import org.plank.analyzer.element.ResolvedModuleDecl
import org.plank.analyzer.element.ResolvedNoBody
import org.plank.analyzer.element.ResolvedPlankFile
import org.plank.analyzer.element.ResolvedReturnStmt
import org.plank.analyzer.element.ResolvedStmt
import org.plank.analyzer.element.ResolvedStructDecl
import org.plank.analyzer.element.ResolvedUseDecl
import org.plank.analyzer.element.TypedAccessExpr
import org.plank.analyzer.element.TypedAssignExpr
import org.plank.analyzer.element.TypedBlockExpr
import org.plank.analyzer.element.TypedCallExpr
import org.plank.analyzer.element.TypedConstExpr
import org.plank.analyzer.element.TypedDerefExpr
import org.plank.analyzer.element.TypedErrorExpr
import org.plank.analyzer.element.TypedExpr
import org.plank.analyzer.element.TypedGetExpr
import org.plank.analyzer.element.TypedGroupExpr
import org.plank.analyzer.element.TypedIdentPattern
import org.plank.analyzer.element.TypedIfExpr
import org.plank.analyzer.element.TypedInstanceExpr
import org.plank.analyzer.element.TypedMatchExpr
import org.plank.analyzer.element.TypedNamedTuplePattern
import org.plank.analyzer.element.TypedPattern
import org.plank.analyzer.element.TypedRefExpr
import org.plank.analyzer.element.TypedSetExpr
import org.plank.analyzer.element.TypedSizeofExpr
import org.plank.analyzer.element.TypedViolatedPattern
import org.plank.syntax.element.Identifier
import org.plank.syntax.element.QualifiedPath

@Suppress("TooManyFunctions")
open class IrTransformingPhase :
  TypedExpr.Visitor<TypedExpr>,
  ResolvedStmt.Visitor<ResolvedStmt>,
  TypedPattern.Visitor<TypedPattern>,
  ResolvedPlankFile.Visitor<ResolvedPlankFile>,
  ResolvedFunctionBody.Visitor<ResolvedFunctionBody>,
  Identifier.Visitor<Identifier>,
  QualifiedPath.Visitor<QualifiedPath> {
  open fun transformNoBody(body: ResolvedNoBody): ResolvedFunctionBody {
    return body
  }

  open fun transformExprBody(body: ResolvedExprBody): ResolvedFunctionBody {
    return body
  }

  open fun transformCodeBody(body: ResolvedCodeBody): ResolvedFunctionBody {
    return body
  }

  open fun transformPlankFile(file: ResolvedPlankFile): ResolvedPlankFile {
    return file
  }

  open fun transformExprStmt(stmt: ResolvedExprStmt): ResolvedStmt {
    return stmt
  }

  open fun transformReturnStmt(stmt: ResolvedReturnStmt): ResolvedStmt {
    return stmt
  }

  open fun transformUseDecl(decl: ResolvedUseDecl): ResolvedStmt {
    return decl
  }

  open fun transformModuleDecl(decl: ResolvedModuleDecl): ResolvedStmt {
    return decl
  }

  open fun transformEnumDecl(decl: ResolvedEnumDecl): ResolvedStmt {
    return decl
  }

  open fun transformStructDecl(decl: ResolvedStructDecl): ResolvedStmt {
    return decl
  }

  open fun transformFunDecl(decl: ResolvedFunDecl): ResolvedStmt {
    return decl
  }

  open fun transformLetDecl(decl: ResolvedLetDecl): ResolvedStmt {
    return decl
  }

  open fun transformViolatedStmt(stmt: ResolvedErrorStmt): ResolvedStmt {
    return stmt
  }

  open fun transformViolatedDecl(stmt: ResolvedErrorDecl): ResolvedStmt {
    return stmt
  }

  open fun transformBlockExpr(expr: TypedBlockExpr): TypedExpr {
    return expr
  }

  open fun transformConstExpr(expr: TypedConstExpr): TypedExpr {
    return expr
  }

  open fun transformIfExpr(expr: TypedIfExpr): TypedExpr {
    return expr
  }

  open fun transformAccessExpr(expr: TypedAccessExpr): TypedExpr {
    return expr
  }

  open fun transformCallExpr(expr: TypedCallExpr): TypedExpr {
    return expr
  }

  open fun transformAssignExpr(expr: TypedAssignExpr): TypedExpr {
    return expr
  }

  open fun transformSetExpr(expr: TypedSetExpr): TypedExpr {
    return expr
  }

  open fun transformGetExpr(expr: TypedGetExpr): TypedExpr {
    return expr
  }

  open fun transformGroupExpr(expr: TypedGroupExpr): TypedExpr {
    return expr
  }

  open fun transformInstanceExpr(expr: TypedInstanceExpr): TypedExpr {
    return expr
  }

  open fun transformSizeofExpr(expr: TypedSizeofExpr): TypedExpr {
    return expr
  }

  open fun transformReferenceExpr(expr: TypedRefExpr): TypedExpr {
    return expr
  }

  open fun transformDerefExpr(expr: TypedDerefExpr): TypedExpr {
    return expr
  }

  open fun transformMatchExpr(expr: TypedMatchExpr): TypedExpr {
    return expr
  }

  open fun transformViolatedExpr(expr: TypedErrorExpr): TypedExpr {
    return expr
  }

  open fun transformNamedTuplePattern(pattern: TypedNamedTuplePattern): TypedPattern {
    return pattern
  }

  open fun transformIdentPattern(pattern: TypedIdentPattern): TypedPattern {
    return pattern
  }

  open fun transformViolatedPattern(pattern: TypedViolatedPattern): TypedPattern {
    return pattern
  }

  open fun transformIdentifier(identifier: Identifier): Identifier {
    return identifier
  }

  open fun transformQualifiedPath(path: QualifiedPath): QualifiedPath {
    return path
  }

  final override fun visitNoBody(body: ResolvedNoBody): ResolvedFunctionBody {
    return transformNoBody(body)
  }

  final override fun visitExprBody(body: ResolvedExprBody): ResolvedFunctionBody {
    visit(body.expr)

    return transformExprBody(body)
  }

  final override fun visitCodeBody(body: ResolvedCodeBody): ResolvedFunctionBody {
    visitStmts(body.stmts)
    body.returned?.let { visit(it) }

    return transformCodeBody(body)
  }

  final override fun visitPlankFile(file: ResolvedPlankFile): ResolvedPlankFile {
    visitStmts(file.program)

    return transformPlankFile(file)
  }

  final override fun visitExprStmt(stmt: ResolvedExprStmt): ResolvedStmt {
    visit(stmt.expr)

    return transformExprStmt(stmt)
  }

  final override fun visitReturnStmt(stmt: ResolvedReturnStmt): ResolvedStmt {
    stmt.value?.let { visit(it) }

    return transformReturnStmt(stmt)
  }

  final override fun visitUseDecl(decl: ResolvedUseDecl): ResolvedStmt {
    return transformUseDecl(decl)
  }

  final override fun visitModuleDecl(decl: ResolvedModuleDecl): ResolvedStmt {
    visit(decl.name)
    visitStmts(decl.content)

    return transformModuleDecl(decl)
  }

  final override fun visitEnumDecl(decl: ResolvedEnumDecl): ResolvedStmt {
    visit(decl.name)
    decl.members.values.forEach { member ->
      visit(member.name)
    }

    return transformEnumDecl(decl)
  }

  final override fun visitStructDecl(decl: ResolvedStructDecl): ResolvedStmt {
    visit(decl.name)
    decl.properties.values.forEach { property ->
      visit(property.name)
      property.value?.let { visit(it) }
    }

    return transformStructDecl(decl)
  }

  final override fun visitFunDecl(decl: ResolvedFunDecl): ResolvedStmt {
    visit(decl.name)
    visit(decl.body)
    decl.realParameters.keys.forEach { visit(it) }

    return transformFunDecl(decl)
  }

  final override fun visitLetDecl(decl: ResolvedLetDecl): ResolvedStmt {
    visit(decl.name)
    visit(decl.value)

    return transformLetDecl(decl)
  }

  final override fun visitViolatedStmt(stmt: ResolvedErrorStmt): ResolvedStmt {
    return transformViolatedStmt(stmt)
  }

  final override fun visitViolatedDecl(stmt: ResolvedErrorDecl): ResolvedStmt {
    return transformViolatedDecl(stmt)
  }

  final override fun visitBlockExpr(expr: TypedBlockExpr): TypedExpr {
    visitStmts(expr.stmts)
    visit(expr.returned)

    return transformBlockExpr(expr)
  }

  final override fun visitConstExpr(expr: TypedConstExpr): TypedExpr {
    return transformConstExpr(expr)
  }

  final override fun visitIfExpr(expr: TypedIfExpr): TypedExpr {
    visit(expr.cond)
    visit(expr.thenBranch)
    expr.elseBranch?.let { visit(it) }

    return transformIfExpr(expr)
  }

  final override fun visitAccessExpr(expr: TypedAccessExpr): TypedExpr {
    visit(expr.name)
    visit(expr.variable.name)
    visit(expr.variable.value)

    return transformAccessExpr(expr)
  }

  final override fun visitCallExpr(expr: TypedCallExpr): TypedExpr {
    visit(expr.callee)
    expr.arguments.forEach { visit(it) }

    return transformCallExpr(expr)
  }

  final override fun visitAssignExpr(expr: TypedAssignExpr): TypedExpr {
    visit(expr.name)
    visit(expr.value)

    return transformAssignExpr(expr)
  }

  final override fun visitSetExpr(expr: TypedSetExpr): TypedExpr {
    visit(expr.receiver)
    visit(expr.member)
    visit(expr.value)

    return transformSetExpr(expr)
  }

  final override fun visitGetExpr(expr: TypedGetExpr): TypedExpr {
    visit(expr.receiver)
    visit(expr.member)

    return transformGetExpr(expr)
  }

  final override fun visitGroupExpr(expr: TypedGroupExpr): TypedExpr {
    visit(expr.expr)

    return transformGroupExpr(expr)
  }

  final override fun visitInstanceExpr(expr: TypedInstanceExpr): TypedExpr {
    expr.arguments.forEach { (name, value) ->
      visit(name)
      visit(value)
    }

    return transformInstanceExpr(expr)
  }

  final override fun visitSizeofExpr(expr: TypedSizeofExpr): TypedExpr {
    return transformSizeofExpr(expr)
  }

  final override fun visitReferenceExpr(expr: TypedRefExpr): TypedExpr {
    visit(expr.expr)

    return transformReferenceExpr(expr)
  }

  final override fun visitDerefExpr(expr: TypedDerefExpr): TypedExpr {
    visit(expr.expr)

    return transformDerefExpr(expr)
  }

  final override fun visitMatchExpr(expr: TypedMatchExpr): TypedExpr {
    visit(expr.subject)
    expr.patterns.forEach { (pattern, value) ->
      visit(pattern)
      visit(value)
    }

    return transformMatchExpr(expr)
  }

  final override fun visitViolatedExpr(expr: TypedErrorExpr): TypedExpr {
    return transformViolatedExpr(expr)
  }

  final override fun visitNamedTuplePattern(pattern: TypedNamedTuplePattern): TypedPattern {
    pattern.properties.forEach { visit(it) }

    return transformNamedTuplePattern(pattern)
  }

  final override fun visitIdentPattern(pattern: TypedIdentPattern): TypedPattern {
    visit(pattern.name)

    return transformIdentPattern(pattern)
  }

  final override fun visitViolatedPattern(pattern: TypedViolatedPattern): TypedPattern {
    return transformViolatedPattern(pattern)
  }

  final override fun visitIdentifier(identifier: Identifier): Identifier {
    return transformIdentifier(identifier)
  }

  final override fun visitQualifiedPath(path: QualifiedPath): QualifiedPath {
    path.fullPath.forEach { visit(it) }

    return transformQualifiedPath(path)
  }
}
