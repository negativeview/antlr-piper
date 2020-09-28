package com.gracefulcode.piper.parsed;

import com.gracefulcode.piper.generated.PiperParser;

import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

class Expression {
    protected PiperParser.ExpressionContext context;
    protected LLVMValueRef value;

    public Expression(PiperParser.ExpressionContext context, VariableContext variableContext, DataType goalType) {
        this.context = context;
        if (context.left_expression() != null) {
            LeftExpression leftExpression = new LeftExpression(context.left_expression(), variableContext);
        } else if (context.DOUBLE_DOT() != null) {
            System.out.println("Got double dot");
        } else if (context.NUMBER(0) != null) {
            if (LLVMGetTypeKind(goalType.getLLVMType()) == LLVMIntegerTypeKind) {
                this.value = LLVMConstInt(
                    LLVMIntType(
                        LLVMGetIntTypeWidth(goalType.getLLVMType())
                    ),
                    Integer.parseInt(context.NUMBER(0).getText()),
                    0
                );
            }
        } else {
            System.out.println("Out of ideas: " + context.getText());
        }
    }

    public LLVMValueRef getValue() {
        return this.value;
    }

    public static class LeftExpression {
        protected PiperParser.Left_expressionContext context;

        public LeftExpression(PiperParser.Left_expressionContext context, VariableContext variableContext) {
            this.parse(context, variableContext);
        }

        protected void parse(PiperParser.Left_expressionContext context, VariableContext variableContext) {
            this.context = context;
            System.out.println("LEFT EXPRESSION: " + this.context.getText());

            if (this.context.DOT() != null) {
                // left_expression DOT ID
            } else if (this.context.ID() != null) {
                // ID -- terminal
                VariableContext.Variable variable = variableContext.getVariable(this.context.ID().getText());
                if (variable == null) {
                    System.out.println(
                        "JUST ID, NOT FOUND: " + this.context.ID().getText()
                    );
                } else {
                    System.out.println(
                        "JUST ID: " + this.context.ID().getText() + " -> " + variableContext.getVariable(this.context.ID().getText()).toString()
                    );
                }
            } else if (this.context.expression() != null) {
                // left_expression LSQUARE expression RSQUARE
            } else if (this.context.left_expression() != null) {
                // '(' left_expression ')'
                // All we need to do is pretend like the parens aren't there
                this.parse(this.context.left_expression(), variableContext);
            } else {
                // WHAT?!
            }
        }

        public DataType getDataType() {
            return null;
        }
    }
}