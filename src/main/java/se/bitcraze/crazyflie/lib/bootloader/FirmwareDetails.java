package se.bitcraze.crazyflie.lib.bootloader;

/**
 * Needs to be in a separate class, otherwise more JSON serialization magic is required
 *
 */
public class FirmwareDetails {
    private String mPlatform;
    private String mTarget;
    private String mType;
    private String mRelease;
    private String mRepository;

    public FirmwareDetails() {
    }

    public FirmwareDetails(String platform, String target, String type, String release, String repository) {
        this.mPlatform = platform;
        this.mTarget = target;
        this.mType = type;
        this.mRelease = release;
        this.mRepository = repository;
    }

    public String getPlatform() {
        return mPlatform;
    }

    public void setPlatform(String platform) {
        this.mPlatform = platform;
    }

    public String getTarget() {
        return mTarget;
    }

    public void setTarget(String target) {
        this.mTarget = target;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public String getRelease() {
        return mRelease;
    }

    public void setRelease(String mRelease) {
        this.mRelease = mRelease;
    }

    public String getRepository() {
        return mRepository;
    }

    public void setRepository(String mRepository) {
        this.mRepository = mRepository;
    }

    @Override
    public String toString() {
        return "FirmwareDetails [Platform=" + mPlatform + ", Target=" + mTarget + ", Type=" + mType + ", Release=" + mRelease + ", Repository=" + mRepository + "]";
    }
}