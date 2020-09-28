package com.gracefulcode.piper.parsed;

import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class DataTypeI8 implements DataType {
    @Override
    public String getName() {
        return "i8";
    }

    @Override
    public LLVMTypeRef getLLVMType() {
        return LLVMInt8Type();
    }

    @Override
    public boolean shouldBePointer() {
        return false;
    }
}