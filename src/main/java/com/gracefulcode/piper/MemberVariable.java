package com.gracefulcode.piper;

public class MemberVariable {
    protected boolean isInitialized;
    protected boolean isConstrained;
    protected String name;
    protected String dataType;
    protected Integer min;
    protected Integer max;
    protected int initializer;

    public MemberVariable() {
        this.isInitialized = false;
        this.isConstrained = false;
    }

    public void setMin(String min) {
        this.isConstrained = true;
        this.min = Integer.parseInt(min);
    }

    public void setInitializer(int initializer) {
        this.isInitialized = true;
        this.initializer = initializer;
    }

    public boolean getIsInitialized() {
        return this.isInitialized;
    }

    public int getInitializer() {
        return this.initializer;
    }

    public void setMax(String max) {
        this.isConstrained = true;
        this.max = Integer.parseInt(max);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return this.dataType;
    }

    public String toString() {
        String ret = this.name + ":" + this.dataType;
        if (this.isConstrained) {
            ret += " constrained(" + this.min + "-" + this.max + ")";
        }
        return ret;
    }
}