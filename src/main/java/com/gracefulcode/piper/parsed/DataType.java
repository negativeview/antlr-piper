package com.gracefulcode.piper.parsed;

import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public interface DataType {
    String getName();
    LLVMTypeRef getLLVMType();
    boolean shouldBePointer();


    // LLVMGetIntTypeWidth
    // LLVMGetTypeKind
    // LLVMSizeOf
    // LLVMSizeOfTypeInBits
    // LLVMTypeOf
}