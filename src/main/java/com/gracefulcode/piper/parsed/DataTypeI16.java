package com.gracefulcode.piper.parsed;

import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class DataTypeI16 implements DataType {
    @Override
    public String getName() {
        return "i16";
    }

    @Override
    public LLVMTypeRef getLLVMType() {
        return LLVMInt16Type();
    }

    @Override
    public boolean shouldBePointer() {
        return false;
    }
}