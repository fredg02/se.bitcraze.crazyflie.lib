package se.bitcraze.crazyflie.lib.log;

import se.bitcraze.crazyflie.lib.toc.VariableType;

/**
 * A LogVariable is an element of a {@link LogConfig}
 *
 * Instead of fetch_as/stored_as this class uses VariableType (not to be confused with LogVariable.getType())
 *
 * TODO: does LogVariable need to store the ID?
 */
public class LogVariable {

    public final static int TOC_TYPE = 0;
    public final static int MEM_TYPE = 1;

    private String mName;
    private VariableType mVariableType;
    private int mType = TOC_TYPE; // default is TOC_TYPE
    private int mAddress = 0; //TODO: long?

    public LogVariable(String name) {
        this.mName = name;
    }

    public LogVariable(String name, VariableType varType) {
        this(name);
        this.mVariableType = varType;
    }

    public LogVariable(String name, VariableType varType, int type) {
        this(name, varType);
        this.mType = type;
    }

    public LogVariable(String name, VariableType varType, int type, int address) {
        this(name, varType, type);
        this.mAddress = address;
    }

    public String getName() {
        return this.mName;
    }

    public VariableType getVariableType() {
        return this.mVariableType;
    }

    public void setVariableType(VariableType varType) {
        this.mVariableType = varType;
    }

    public int getType() {
        return this.mType;
    }

    public int getAddress() {
        return this.mAddress;
    }

    /**
     * Return true if the variable should be in the TOC, false if raw memory variable
     *
     * @return
     */
    public boolean isTocVariable() {
        return this.mType == LogVariable.TOC_TYPE;
    }

    public String toString() {
        return "LogVariable : name: " + this.mName + ", variableType: " + this.mVariableType;
    }

}
