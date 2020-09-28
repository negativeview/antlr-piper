package com.gracefulcode.piper.parsed;

import com.gracefulcode.piper.generated.PiperParser;
import com.gracefulcode.piper.generated.PiperLexer;

import java.util.HashMap;
import java.util.List;

import org.bytedeco.javacpp.*;
import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

import org.antlr.v4.runtime.tree.TerminalNode;

public class Function {
    PiperParser.Function_definitionContext context;
    String name;
    LLVMTypeRef outputLLVMType;
    DataType outputType;
    LLVMValueRef baseFunction;

    public Function(PiperParser.Function_definitionContext context, HashMap<String, DataType> structs) {
        this.context = context;
        this.name = context.function_header().ID().getText();

        this.parseArgs();
        this.parseOut(structs);
    }

    protected void parseArgs() {
        PiperParser.Function_arg_definitionsContext argDefinitions =
            this.context.function_header().function_arg_definitions();

        if (argDefinitions.function_arg_definition() != null) {
            for (PiperParser.Function_arg_definitionContext arg: argDefinitions.function_arg_definition()) {
                System.out.println("ARG: " + arg.ID(0).getText() + " -> " + arg.ID(1).getText());
            }
            // list of Function_arg_definitionContext's
            // which are ID(0) and ID(1)
        } else {
            // single struct
        }

        System.out.println("ARGS: " + argDefinitions.getText());
    }

    protected void parseOut(HashMap<String, DataType> structs) {
        PiperParser.Function_out_definitionsContext out = this.context.function_header().function_out_definitions();
        if (out == null) {
            this.outputLLVMType = LLVMVoidType();
            return;
        }

        if (!structs.containsKey(out.getText())) throw new RuntimeException("No such struct: " + out.getText());

        this.outputType = structs.get(out.getText());
        if (this.outputType.shouldBePointer()) {
            this.outputLLVMType = LLVMPointerType(this.outputType.getLLVMType(), 0);
        } else {
            this.outputLLVMType = this.outputType.getLLVMType();
        }
    }

    public String getName() {
        return this.name;
    }

    /**
     * TODO: I HAVE TO parse the converters first to be able to generate all
     *       of the variants. I need to generate every combination of valid
     *       input and output, taking converters into account.
     */
    public void addDefinitions(LLVMContextRef context, LLVMModuleRef module) {
        this.baseFunction = LLVMAddFunction(
            module,
            "func$" + this.name + "$base",
            LLVMFunctionType(
                this.outputLLVMType,
                (LLVMTypeRef)null,
                0,
                0
            )
        );

        if (this.outputType instanceof Struct) {
            if (((Struct)this.outputType).canBeBased()) {
                this.baseFunction = LLVMAddFunction(
                    module,
                    "func$" + this.name + "$simplified",
                    LLVMFunctionType(
                        ((Struct)this.outputType).getBase().getLLVMType(),
                        (LLVMTypeRef)null,
                        0,
                        0
                    )
                );        
            }
        }
    }
}