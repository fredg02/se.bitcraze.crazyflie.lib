package se.bitcraze.crazyflie.lib.toc;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * An element in the TOC
 *
 */
public class TocElement {

    public static int RW_ACCESS = 1;
    public static int RO_ACCESS = 0;

    private int mIdent = 0;
    private String mGroup = "";
    private String mName = "";
    private VariableType mCtype;
    private String mPytype = "";
    private int mAccess = RO_ACCESS;

    public TocElement() {

    }

    public int getIdent() {
        return mIdent;
    }

    public void setIdent(int ident) {
        this.mIdent = ident;
    }

    public String getGroup() {
        return mGroup;
    }

    public void setGroup(String group) {
        this.mGroup = group;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    @JsonIgnore
    public String getCompleteName() {
        return mGroup + "." + mName;
    }

    public VariableType getCtype() {
        return mCtype;
    }

    public void setCtype(VariableType ctype) {
        this.mCtype = ctype;
    }

    public String getPytype() {
        return mPytype;
    }

    public void setPytype(String pytype) {
        this.mPytype = pytype;
    }

    public int getAccess() {
        return mAccess;
    }

    public void setAccess(int access) {
        this.mAccess = access;
    }

    @Override
    public String toString() {
        return "Ident: " + getIdent() + ", Group: " + getGroup() + ", Name: " + getName() + ", Ctype: " + getCtype() + ", Access: " + (getAccess() == 0 ? "RO" : "RW");
    }
}
