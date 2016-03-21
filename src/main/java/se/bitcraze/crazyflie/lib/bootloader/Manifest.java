package se.bitcraze.crazyflie.lib.bootloader;

import java.util.HashMap;
import java.util.Map;


public class Manifest {
    private int mVersion;
    private int mSubversion;
    private String mRelease;
    private Map<String, FirmwareDetails> mFiles = new HashMap<String, FirmwareDetails>();

    public Manifest() {

    }

    public Manifest(int version, int subversion, String release, Map<String, FirmwareDetails> files) {
        this.mVersion = version;
        this.mSubversion = subversion;
        this.mRelease = release;
        this.mFiles = files;
    }

    public int getVersion() {
        return mVersion;
    }

    public void setVersion(int version) {
        this.mVersion = version;
    }

    public int getSubversion() {
        return mSubversion;
    }

    public void setSubversion(int mSubVersion) {
        this.mSubversion = mSubVersion;
    }

    public String getRelease() {
        return mRelease;
    }

    public void setRelease(String mRelease) {
        this.mRelease = mRelease;
    }

    public Map<String, FirmwareDetails> getFiles() {
        return mFiles;
    }

    public void setFiles(Map<String, FirmwareDetails> files) {
        this.mFiles = files;
    }

}