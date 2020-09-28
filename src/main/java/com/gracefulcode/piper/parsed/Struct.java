package com.gracefulcode.piper.parsed;

import com.gracefulcode.piper.generated.PiperParser;

import org.bytedeco.javacpp.*;
import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Struct implements DataType {
    PiperParser.Struct_blockContext context;
    String name;
    LLVMTypeRef structType;
    LLVMValueRef initializer;
    ArrayList<MemberVariable> memberVariables = new ArrayList<MemberVariable>();

    public Struct(PiperParser.Struct_blockContext context) {
        this.context = context;
        this.name = this.context.ID().getSymbol().getText();
    }

    public int getMemberVariableCount() {
        return this.memberVariables.size();
    }

    public boolean canBeBased() {
        if (this.memberVariables.size() > 1) return false;
        if (this.memberVariables.get(0).getDataType().shouldBePointer() == false) return true;
        if (this.memberVariables.get(0).getDataType() instanceof Struct) {
            return ((Struct)this.memberVariables.get(0).getDataType()).canBeBased();
        } else {
            // This should never happen. If it does, do the "safe" thing?
            return false;
        }
    }

    public DataType getBase() {
        if (!this.canBeBased()) return null;
        if (this.memberVariables.get(0).getDataType().shouldBePointer() == false) return this.memberVariables.get(0).getDataType();
        return ((Struct)this.memberVariables.get(0).getDataType()).getBase();
    }

    @Override
    public LLVMTypeRef getLLVMType() {
        return this.structType;
    }

    LLVMValueRef getInitializer() {
        return this.initializer;
    }

    public String getName() {
        return this.name;
    }

    public void createInitializerDefinition(LLVMModuleRef module) {
        this.initializer = LLVMAddFunction(
            module,
            "struct$" + this.name + "$init",
            LLVMFunctionType(
                LLVMPointerType(this.structType, 0),
                (LLVMTypeRef)null,
                0,
                0
            )
        );
    }

    public void createInitializerBody(LLVMContextRef context, LLVMModuleRef module) {
        LLVMBasicBlockRef entry = LLVMAppendBasicBlock(this.initializer, "entry");
        LLVMBuilderRef builder = LLVMCreateBuilder();
        LLVMPositionBuilderAtEnd(builder, entry);

        LLVMTargetDataRef dataRef = LLVMGetModuleDataLayout(module);
        long size = LLVMABISizeOfType(dataRef, this.structType);

        LLVMValueRef malloc = LLVMGetNamedFunction(module, "malloc");
        LLVMValueRef localPointer = LLVMBuildAlloca(builder, this.structType, "ret");
        LLVMValueRef[] mallocTypes = new LLVMValueRef[1];
        mallocTypes[0] = LLVMConstInt(LLVMInt64Type(), size, 0);
        LLVMValueRef mallocResult = LLVMBuildCall(
            builder,
            malloc,
            new PointerPointer<LLVMValueRef>(mallocTypes),
            1,
            "raw_malloc"
        );
        LLVMValueRef cast = LLVMBuildBitCast(
            builder,
            mallocResult,
            LLVMPointerType(structType, 0),
            "ret"
        );
        LLVMValueRef res = cast;

        LLVMValueRef updated = LLVMBuildLoad(builder, res, "loaded");
        int i = 0;
        for (MemberVariable memberVariable: this.memberVariables) {
            if (memberVariable.getDataType() instanceof Struct) {
                Struct struct = (Struct)memberVariable.getDataType();
                LLVMValueRef callResult = LLVMBuildCall(
                    builder,
                    struct.getInitializer(),
                    null,
                    0,
                    memberVariable.getName()
                );
                // LLVMValueRef gep = LLVMBuildStructGEP(builder, res, i, "gep");
                // LLVMBuildStore(builder, callResult, gep);

                updated = LLVMBuildInsertValue(
                    builder,
                    updated,
                    callResult,
                    i,
                    "ret"
                );
            } else if (memberVariable.getIsInitialized()) {
                updated = LLVMBuildInsertValue(
                    builder,
                    updated,
                    memberVariable.getInitialValue(),
                    i,
                    "init"
                );
            }
            i++;
        }
        LLVMBuildStore(builder, updated, cast);
        LLVMBuildRet(builder, res);
    }

    public void createBase(LLVMContextRef llvmContext) {
        this.structType = LLVMStructCreateNamed(llvmContext, this.name);
    }

    @Override
    public boolean shouldBePointer() {
        return true;
    }

    public void createInner(LLVMContextRef llvmContext, HashMap<String, DataType> structs) {
        ArrayList<LLVMTypeRef> structContents = new ArrayList<LLVMTypeRef>();

        List<PiperParser.Member_variableContext> memberVariables = this.context.member_variables().member_variable();
        for (PiperParser.Member_variableContext mv : memberVariables) {
            MemberVariable memberVariable = new MemberVariable(
                mv,
                structs
            );
            if (memberVariable.getDataType().shouldBePointer()) {
                structContents.add(
                    LLVMPointerType(
                        memberVariable.getDataType().getLLVMType(),
                        0
                    )
                );
            } else {
                structContents.add(
                    memberVariable.getDataType().getLLVMType()
                );
            }
            this.memberVariables.add(memberVariable);
        }

        LLVMTypeRef[] types = new LLVMTypeRef[structContents.size()];
        types = structContents.toArray(types);

        LLVMStructSetBody(
            this.structType,
            new PointerPointer<LLVMTypeRef>(types),
            structContents.size(),
            0
        );
    }
}