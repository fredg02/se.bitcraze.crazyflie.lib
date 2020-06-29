package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;

@Category(OfflineTests.class)
public class ManifestTest {

    @Test
    public void testManifest() {
        int version = 123;
        int subVersion = 254;
        String release = "bla";
        Map<String, FirmwareDetails> files = new HashMap<>();

        Manifest mf = new Manifest(version, subVersion, release, files);
        assertEquals(version, mf.getVersion());
        assertEquals(subVersion, mf.getSubversion());
        assertEquals(release, mf.getRelease());
        assertEquals(files, mf.getFiles());

        Manifest mf2 = new Manifest();
        mf2.setVersion(version);
        mf2.setSubversion(subVersion);
        mf2.setRelease(release);
        mf2.setFiles(files);

        assertEquals(version, mf2.getVersion());
        assertEquals(subVersion, mf2.getSubversion());
        assertEquals(release, mf2.getRelease());
        assertEquals(files, mf2.getFiles());
    }

}
