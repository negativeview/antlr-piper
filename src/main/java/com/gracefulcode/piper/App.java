package com.gracefulcode.piper;

import com.gracefulcode.piper.generated.PiperLexer;
import com.gracefulcode.piper.generated.PiperBaseListener;
import com.gracefulcode.piper.generated.PiperParser;
import com.gracefulcode.piper.parsed.File;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.io.FileOutputStream;

import org.bytedeco.javacpp.*;
import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class App {
    public static interface Statement {
    };

    public static interface Expression {
    };

    public static class LeftExpression implements Expression {
        public LeftExpression(PiperParser.Left_expressionContext context) {
            if (context.DOT() != null) {
                // left_expression DOT ID
                System.out.println("left expression: " + context.getText());
            } else if (context.LSQUARE() != null) {
                // left_expression LSQUARE expression RSQUARE
                System.out.println("left expression: " + context.getText());
            } else if (context.left_expression() != null) {
                // ( left_expression )
                System.out.println("left expression: " + context.getText());
            } else if (context.ID() != null) {
                // ID
                System.out.println("Just ID: " + context.ID().getText());
            } else {
                System.out.println("left expression: " + context.getText());
            }
        }
    };

    public static class AssignmentStatement implements Statement {
        protected LeftExpression left;

        public AssignmentStatement() {

        }

        public void setLeft(PiperParser.Left_expressionContext left) {
            this.left = new LeftExpression(left);
        }

        public void setRight(PiperParser.ExpressionContext right) {
            System.out.println("Set right: " + right.getText());
        }
    };

    public static class Context {
        public HashMap<String, String> variables;

        public Context() {
            this.variables = new HashMap<String, String>();
        }

        public void addVariable(String variableName, String variableType) {
            this.variables.put(variableName, variableType);
        }
    };

    public static class CodeBlock {
        protected Context context;
        protected CodeBlock parent;

        public CodeBlock(Context context) {
            this.context = context;
            this.parent = null;
        }

        public CodeBlock(Context context, CodeBlock parent) {
            this(context);
            this.parent = parent;
        }

        public void parse(PiperParser.Code_blockContext block) {
            for (PiperParser.StatementContext statement: block.statement()) {
                if (statement.if_statement() != null) {
                
                } else if (statement.return_statement() != null) {

                } else if (statement.assignment_statement() != null) {
                    AssignmentStatement assignmentStatement = new AssignmentStatement();
                    assignmentStatement.setLeft(statement.assignment_statement().left_expression());
                    assignmentStatement.setRight(statement.assignment_statement().expression());
                } else if (statement.let_statement() != null) {

                } else if (statement.expression() != null) {

                } else {

                }
            }
        }
    };

    public static void main(String[] args) throws IOException {
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeDisassembler();
        LLVMInitializeNativeTarget();

        LLVMModuleRef mod = LLVMModuleCreateWithName("main");
        LLVMContextRef context = LLVMGetModuleContext(mod);

        LLVMValueRef malloc = LLVMAddFunction(
            mod,
            "malloc",
            LLVMFunctionType(
                LLVMPointerType(
                    LLVMInt8Type(),
                    0
                ),
                LLVMInt64Type(),
                1,
                0
            )
        );

        CharStream fileStream = CharStreams.fromFileName(args[0]);
        PiperLexer piperLexer = new PiperLexer(fileStream);
        CommonTokenStream tokenStream = new CommonTokenStream(piperLexer);
        PiperParser piperParser = new PiperParser(tokenStream);
        
        PiperParser.FileContext fileContext = piperParser.file();
        File file = new File(fileContext);
        file.parseDataTypes(context, mod);
        file.parseCastings(context, mod);
        file.parseFunctions(context, mod);

        BytePointer error = new BytePointer((Pointer)null);

        // LLVMPassManagerRef pass = LLVMCreatePassManager();
        // LLVMAddConstantPropagationPass(pass);
        // LLVMAddInstructionCombiningPass(pass);
        // LLVMAddPromoteMemoryToRegisterPass(pass);
        // LLVMAddGVNPass(pass);
        // LLVMAddCFGSimplificationPass(pass);
        // LLVMRunPassManager(pass, mod);
        // LLVMVerifyModule(mod, LLVMAbortProcessAction, error);
        LLVMDumpModule(mod);

        LLVMExecutionEngineRef engine = new LLVMExecutionEngineRef();
        if (LLVMCreateJITCompilerForModule(engine, mod, 2, error) != 0) {
            System.err.println(error.getString());
            LLVMDisposeMessage(error);
            System.exit(-1);
        }

        // LLVMGenericValueRef exec_args = LLVMCreateGenericValueOfInt(LLVMInt32Type(), 10, 0);
        // LLVMGenericValueRef exec_res = LLVMRunFunction(engine, fac, 1, exec_args);
        // System.err.println();
        // System.err.println("; Running fac(10) with JIT...");
        // System.err.println("; Result: " + LLVMGenericValueToInt(exec_res, 0));
    }
}