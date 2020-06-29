package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;

@Category(OfflineTests.class)
public class FirmwareReleaseTest {

    String tagName = "2017.01";
    String name = "cf1";
    String createdAt = "2017-01-01T14:20:3Z";
    String createdAtParsed = "2017-01-01";

    @Test
    public void testFirmwareRelease() {
        String toString = "Firmware [mTagName=" + tagName + ", mName=" + name + ", mCreatedAt=" + createdAtParsed + "]";

        FirmwareRelease fr = new FirmwareRelease(tagName, name, createdAt);

        assertEquals(tagName, fr.getTagName());
        assertEquals(name, fr.getName());
        assertEquals(createdAtParsed, fr.getCreatedAt());
        assertEquals(toString, fr.toString());

        FirmwareRelease fr2 = new FirmwareRelease(tagName, name, createdAtParsed);
        assertEquals(createdAtParsed, fr2.getCreatedAt());

        String releaseNotes = "Beautiful release notes";
        fr.setReleaseNotes(releaseNotes);
        assertEquals(releaseNotes, fr.getReleaseNotes());

        assertEquals(0, fr.compareTo(new FirmwareRelease("2017.01", name, createdAt)));
        assertEquals(-1, fr.compareTo(new FirmwareRelease("2017.02", name, createdAt)));
    }

    @Test
    public void testFirmwareReleaseAssets() {
        FirmwareRelease fr = new FirmwareRelease(tagName, name, createdAt);

        String assetName = "cf1.zip";
        int assetSize = 2048;
        String url = "https://download.url";

        fr.setAsset(assetName, assetSize, url);

        assertEquals(assetName, fr.getAssetName());
        assertEquals(assetSize, fr.getAssetSize());
        assertEquals(url, fr.getBrowserDownloadUrl());

        assertEquals("CF1", fr.getType());
        fr.setAsset("crazyflie1.zip", assetSize, url);
        assertEquals("CF1", fr.getType());

        fr.setAsset("cf2.zip", assetSize, url);
        assertEquals("CF2", fr.getType());
        fr.setAsset("crazyflie2.zip", assetSize, url);
        assertEquals("CF2", fr.getType());
        fr.setAsset("cflie2.zip", assetSize, url);
        assertEquals("CF2", fr.getType());

        fr.setAsset("crazyflie-firmware.zip", assetSize, url);
        assertEquals("CF1 & CF2", fr.getType());

        fr.setAsset("boing.zip", assetSize, url);
        assertEquals("Unknown", fr.getType());
    }

}
