package com.gabrielleeg1.plank.grammar.debug

import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties

internal actual fun List<*>.asMap(): Map<String, Any?> {
  return withIndex().associate { (index, value) ->
    index.toString() to value
  }
}

internal actual fun Any?.asMap(): Map<String, Any?> {
  if (this == null) return mapOf()

  return this::class.memberProperties
    .filterNot { it.hasAnnotation<DontDump>() }
    .associate { property ->
      property.name to property.call(this)
    }
}
