package com.gracefulcode.piper.parsed;

import com.gracefulcode.piper.generated.PiperParser;

public class CodeBlock {
    PiperParser.Code_blockContext context;

    public CodeBlock(PiperParser.Code_blockContext context) {
        this.context = context;

        for (PiperParser.StatementContext stmt: context.statement()) {
            Statement statement = new Statement(stmt);
        }
    }
}