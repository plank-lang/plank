package com.gabrielleeg1.plank.grammar.element

import com.gabrielleeg1.plank.grammar.element.Location.Companion.undefined

class Identifier(val text: String, override val location: Location) : PlankElement {
  interface Visitor<T> {
    fun visit(identifier: Identifier): T = identifier.accept(this)

    fun visitIdentifier(identifier: Identifier): T
  }

  fun <T> accept(visitor: Visitor<T>): T {
    return visitor.visitIdentifier(this)
  }

  override fun toString(): String = "Identifier($text)"

  companion object {
    fun of(text: String, location: Location = undefined()): Identifier {
      return Identifier(text, location)
    }

    fun eq(location: Location = undefined()): Identifier = of("==", location)
    fun neq(location: Location = undefined()): Identifier = of("!=", location)
    fun gt(location: Location = undefined()): Identifier = of(">", location)
    fun gte(location: Location = undefined()): Identifier = of(">=", location)
    fun lte(location: Location = undefined()): Identifier = of("<=", location)
    fun lt(location: Location = undefined()): Identifier = of("<", location)

    fun add(location: Location = undefined()): Identifier = of("+", location)
    fun sub(location: Location = undefined()): Identifier = of("-", location)
    fun times(location: Location = undefined()): Identifier = of("*", location)
    fun div(location: Location = undefined()): Identifier = of("/", location)

    fun concat(location: Location = undefined()): Identifier = of("++", location)
  }
}
