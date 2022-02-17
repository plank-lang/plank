package org.plank.codegen

import org.plank.analyzer.infer.ConstTy
import org.plank.analyzer.infer.FunTy
import org.plank.analyzer.infer.PtrTy
import org.plank.analyzer.infer.Ty
import org.plank.analyzer.infer.boolTy
import org.plank.analyzer.infer.charTy
import org.plank.analyzer.infer.i16Ty
import org.plank.analyzer.infer.i32Ty
import org.plank.analyzer.infer.i8Ty
import org.plank.analyzer.infer.unitTy
import org.plank.llvm4k.ir.Type
import org.plank.llvm4k.ir.FunctionType as LLVMFunctionType

@Suppress("Detekt.ComplexMethod")
fun CodegenContext.typegen(ty: Ty): Type {
  return when (ty) {
    unitTy -> unit
    boolTy -> i1
    charTy -> i8
    i8Ty -> i8
    i16Ty -> i16
    i32Ty -> i32
    is ConstTy -> findStruct(ty.name) ?: codegenError("Unresolved type `${ty.name}`")
    is PtrTy -> ty.arg.typegen().pointer()
    is FunTy -> {
      val returnTy = ty.returnTy.typegen()
      val parameterTy = ty.parameterTy.typegen()

      val functionType = if (parameterTy.kind == Type.Kind.Void) {
        LLVMFunctionType(returnTy, i8.pointer())
      } else {
        LLVMFunctionType(returnTy, i8.pointer(), parameterTy)
      }

      getOrCreateStruct("$ty") {
        elements = listOf(functionType.pointer(), i8.pointer())
      }
    }
    else -> codegenError("Unsupported type `$ty`")
  }
}
