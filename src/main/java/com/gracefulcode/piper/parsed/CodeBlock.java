package com.gracefulcode.piper.parsed;

import com.gracefulcode.piper.generated.PiperParser;

public class CodeBlock {
    PiperParser.Code_blockContext context;

    public CodeBlock(PiperParser.Code_blockContext context, VariableContext parentContext) {
        this.context = context;

        VariableContext variableContext = new VariableContext(parentContext);

        for (PiperParser.StatementContext stmt: context.statement()) {
            Statement statement = new Statement(stmt, variableContext);
        }
    }
}