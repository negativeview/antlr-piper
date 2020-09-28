package com.gracefulcode.piper.parsed;

import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class DataTypeI32 implements DataType {
    @Override
    public String getName() {
        return "i32";
    }

    @Override
    public LLVMTypeRef getLLVMType() {
        return LLVMInt32Type();
    }

    @Override
    public boolean shouldBePointer() {
        return false;
    }
}