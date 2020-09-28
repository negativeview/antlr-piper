package com.gracefulcode.piper.parsed;

import com.gracefulcode.piper.generated.PiperParser;

import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

import java.util.HashMap;

public class MemberVariable {
    PiperParser.Member_variableContext context;
    String name;
    DataType type;
    LLVMValueRef initializedTo;
    boolean isInitialized;

    public MemberVariable(
        PiperParser.Member_variableContext context,
        HashMap<String, DataType> structs
    ) {
        this.context = context;
        this.isInitialized = false;

        if (this.context.member_uninitialized_variable() != null) {
            this.parseBaseVariableDefinition(
                this.context.member_uninitialized_variable(),
                structs
            );
        } else if (this.context.member_initialized_variable() != null) {
            this.isInitialized = true;
            this.parseBaseVariableDefinition(
                this.context.member_initialized_variable(),
                structs
            );
            this.parseInitialization(
                this.context.member_initialized_variable()
            );
        } else {
            throw new RuntimeException("Invalid member variable.");
        }
    }

    public String getName() {
        return this.name;
    }

    public DataType getDataType() {
        return this.type;
    }

    public boolean getIsInitialized() {
        return this.isInitialized;
    }

    public LLVMValueRef getInitialValue() {
        return this.initializedTo;
    }

    protected void parseBaseVariableDefinition(
        PiperParser.Member_uninitialized_variableContext context,
        HashMap<String, DataType> structs
    ) {
        this.name = context.ID(0).getSymbol().getText();
        String proposedDataType = context.ID(1).getSymbol().getText();
        if (!structs.containsKey(proposedDataType)) {
            throw new RuntimeException("Unknown data type");
        }
        this.type = structs.get(proposedDataType);
    }

    protected void parseBaseVariableDefinition(
        PiperParser.Member_initialized_variableContext context,
        HashMap<String, DataType> structs
    ) {
        this.name = context.ID(0).getSymbol().getText();
        String proposedDataType = context.ID(1).getSymbol().getText();
        if (!structs.containsKey(proposedDataType)) {
            throw new RuntimeException("Unknown data type");
        }
        this.type = structs.get(proposedDataType);
    }

    protected void parseInitialization(
        PiperParser.Member_initialized_variableContext context
    ) {
        Expression expression = new Expression(
            context.expression(),
            this.type
        );
        this.initializedTo = expression.getValue();
    }
}