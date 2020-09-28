package com.gracefulcode.piper.parsed;

import java.util.HashMap;

public class VariableContext {
    protected HashMap<String, Variable> variables = new HashMap<String, Variable>();
    protected VariableContext parent;

    public VariableContext() {

    }

    public VariableContext(VariableContext parent) {
        this.parent = parent;
    }

    public Variable getVariable(String name) {
        if (this.variables.containsKey(name)) return this.variables.get(name);
        if (this.parent != null) return this.parent.getVariable(name);
        return null;
    }

    public void addUninitializedVariable(String name, DataType type) {
        this.variables.put(name, new Variable(name, type));
    }

    public static class Variable {
        protected String name;
        protected DataType dataType;

        public Variable(String name, DataType dataType) {
            this.name = name;
            this.dataType = dataType;
        }
    }
}