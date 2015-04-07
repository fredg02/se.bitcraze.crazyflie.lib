package se.bitcraze.crazyflie.lib.param;

public abstract class ParamListener {

    private String mGroup;
    private String mName;

    public ParamListener(String group, String name) {
        this.mGroup = group;
        this.mName = name;
    }

    public String getGroup() {
        return mGroup;
    }

    public String getName() {
        return mName;
    }

    public String getCompleteName() {
        return mGroup + "." + mName;
    }

    public abstract void updated(String name, Number value);

}
