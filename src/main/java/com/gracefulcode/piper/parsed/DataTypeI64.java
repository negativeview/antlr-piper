package com.gracefulcode.piper.parsed;

import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class DataTypeI64 implements DataType {
    @Override
    public String getName() {
        return "i64";
    }

    @Override
    public LLVMTypeRef getLLVMType() {
        return LLVMInt64Type();
    }

    @Override
    public boolean shouldBePointer() {
        return false;
    }
}