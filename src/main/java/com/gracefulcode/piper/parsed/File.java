package com.gracefulcode.piper.parsed;

import com.gracefulcode.piper.generated.PiperParser;

import java.util.HashMap;
import java.util.List;

import org.bytedeco.javacpp.*;
import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

/**
 * A file represents a single file of source code. This is the entry point.
 * 
 * For now, we have a few built-in limitations that will be lifted soon. Here
 * are the limitations and the planned lifting methodology:
 * 
 * 1) We only parse a single file. The entire reason this class exists is to
 *    help ease that eventually, by using this class multiple times for
 *    multiple files.
 * 2) A file is its own implicit namespace. We are not currently parsing any
 *    sort of namespace decorator. We need to add one of those, then use the
 *    same namespace for files in the exact same namespace. We can also use
 *    namespaces to resolve relative imports in step 1).
 * 3) We currently are not parsing classes at all. We need to add class
 *    parsing. By the time we do this it will probably not add much -- we will
 *    already be parsing arbitrary code.
 */
public class File {
    /**
     * Keep a reference to the context that we are parsing. This allows us to
     * run parsing logic in multiple phases.
     */
    protected PiperParser.FileContext fileContext;

    /**
     * This is our implicit namespace. For now it's only structs, but we will
     * need to make this namespace shared between structs and methods.
     */
    protected HashMap<String, DataType> structs = new HashMap<String, DataType>();
    protected HashMap<String, Function> functions = new HashMap<String, Function>();

    public File(PiperParser.FileContext fileContext) {
        this.fileContext = fileContext;
    }

    /**
     * In order to allow types to refer to eachother (and themselves!) without
     * being order-dependent (which sucks), we need to parse their names first,
     * generate the bare minimum of them, and then generate the body of the
     * struct as a second step. While the details of that are hidden in the
     * Struct class, the two-step nature has to be exposed here.
     */
    public void parseDataTypes(
        LLVMContextRef llvmContext,
        LLVMModuleRef module
    ) {
        List<PiperParser.Struct_blockContext> structs = this.fileContext.struct_block();

        this.structs.put("i8", new DataTypeI8());
        this.structs.put("i16", new DataTypeI16());
        this.structs.put("i32", new DataTypeI32());
        this.structs.put("i64", new DataTypeI64());

        for (PiperParser.Struct_blockContext blockContext : structs) {
            /**
             * We very purposefully let the struct give us its name rather than
             * parsing that ourselves. Both for separation of concerns, but
             * also because structs can parse what namespace they are in using
             * whatever logic they want -- the file doesn't need to know about
             * that.
             */
            Struct struct = new Struct(blockContext);
            if (this.structs.containsKey(struct.getName())) {
                throw new RuntimeException("Duplicate definition of " + struct.getName());
            }
            this.structs.put(struct.getName(), struct);
        }

        for (DataType struct: this.structs.values()) {
            if (struct instanceof Struct)
                ((Struct)struct).createBase(llvmContext);
        }
        for (DataType struct: this.structs.values()) {
            if (struct instanceof Struct)
                ((Struct)struct).createInner(llvmContext, this.structs);
        }
        for (DataType struct: this.structs.values()) {
            if (struct instanceof Struct)
                ((Struct)struct).createInitializerDefinition(module);
        }
        for (DataType struct: this.structs.values()) {
            if (struct instanceof Struct)
                ((Struct)struct).createInitializerBody(llvmContext, module);
        }
    }

    public void parseCastings(
        LLVMContextRef llvmContext,
        LLVMModuleRef module
    ) {
        for (PiperParser.Auto_casting_blockContext context: this.fileContext.auto_casting_block()) {
            String from = context.ID(0).getText();
            String to = context.ID(1).getText();
            boolean isExpensive = context.FAT_ARROW() != null;

            CodeBlock codeBlock = new CodeBlock(context.code_block());
        }
    }

    public void parseFunctions(
        LLVMContextRef llvmContext,
        LLVMModuleRef module
    ) {
        for (PiperParser.Function_definitionContext functionDefinitionContext : this.fileContext.function_definition()) {
            Function function = new Function(functionDefinitionContext, this.structs);
            this.functions.put(function.getName(), function);
        }

        for (Function function: this.functions.values()) {
            function.addDefinitions(llvmContext, module);
        }

        // for (PiperParser.Function_definitionContext functionDefinitionContext: this.fileContext.function_definition()) {
        //     System.out.println("HEADER: " + functionDefinitionContext.function_header().getText());
        //     System.out.println("CODE BLOCK: " + functionDefinitionContext.code_block().getText());

        //     for (PiperParser.StatementContext statement: functionDefinitionContext.code_block().statement()) {
        //         if (statement.if_statement() != null) {
        //             if (statement.if_statement().if_statement_without_else() != null) {
        //                 System.out.println("In if w/o else");
        //             } else if (statement.if_statement().if_statement_with_else() != null) {
        //                 PiperParser.ConditionalContext conditional = statement.if_statement().if_statement_with_else().conditional();
        //                 PiperParser.Code_blockContext ifBlock = statement.if_statement().if_statement_with_else().code_block(0);
        //                 PiperParser.Code_blockContext elseBlock = statement.if_statement().if_statement_with_else().code_block(1);

        //                 System.out.println("In if w/ else");
        //                 System.out.println("Conditional: " + conditional.getText());
        //                 System.out.println("If Block: " + ifBlock.getText());
        //                 System.out.println("Else Block: " + elseBlock.getText());
        //             } else {
        //                 System.out.println("In if");
        //             }
        //         } else if (statement.return_statement() != null) {

        //         } else if (statement.assignment_statement() != null) {

        //         } else if (statement.let_statement() != null) {

        //         } else if (statement.expression() != null) {

        //         } else {
        //             System.out.println("STATEMENT: " + statement.getText());
        //         }
        //     }
        // }
    }
}