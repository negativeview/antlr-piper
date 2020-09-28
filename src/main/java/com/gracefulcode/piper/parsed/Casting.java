package com.gracefulcode.piper.parsed;

import com.gracefulcode.piper.generated.PiperParser;

import java.util.HashMap;

public class Casting {
    protected PiperParser.Auto_casting_blockContext context;

    public Casting(PiperParser.Auto_casting_blockContext context, HashMap<String, DataType> structs) {
        this.context = context;

        String from = this.context.ID(0).getText();
        String to = this.context.ID(1).getText();
        boolean isExpensive = this.context.FAT_ARROW() != null;

        // TODO: Need incoming to have storage here.
        VariableContext variableContext = new VariableContext();
        variableContext.addUninitializedVariable("in", structs.get(from));
        variableContext.addUninitializedVariable("out", structs.get(to));

        CodeBlock codeBlock = new CodeBlock(this.context.code_block(), variableContext);
    }
}