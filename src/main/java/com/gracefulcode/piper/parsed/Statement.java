package com.gracefulcode.piper.parsed;

import com.gracefulcode.piper.generated.PiperParser;

public class Statement {
    protected PiperParser.StatementContext context;
    protected SubStatement subStatement;

    public Statement(PiperParser.StatementContext context) {
        this.context = context;

        System.out.println("Statement: " + this.context.getText());
        if (this.context.if_statement() != null) {
            System.out.println("\tIF");
        } else if (this.context.return_statement() != null) {
            System.out.println("\tRETURN");
        } else if (this.context.assignment_statement() != null) {
            this.subStatement = new AssignmentStatement(this.context.assignment_statement());
        } else if (this.context.let_statement() != null) {
            System.out.println("\tLET");
        } else if (this.context.expression() != null) {
            System.out.println("\tEXPRESSION");
        } else {
            System.out.println("\t??!!");
        }
    }

    public static interface SubStatement {

    }

    public static class AssignmentStatement implements SubStatement {
        protected PiperParser.Assignment_statementContext context;

        public AssignmentStatement(PiperParser.Assignment_statementContext context) {
            this.context = context;

            Expression.LeftExpression leftExpression = new Expression.LeftExpression(this.context.left_expression());
            Expression rightExpression = new Expression(
                this.context.expression(),
                leftExpression.getDataType()
            );

            // System.out.println("ASSIGNMENT\n\tLEFT: " + this.context.left_expression().getText() + "\n\tRIGHT: " + this.context.expression().getText());
        }
    }
}