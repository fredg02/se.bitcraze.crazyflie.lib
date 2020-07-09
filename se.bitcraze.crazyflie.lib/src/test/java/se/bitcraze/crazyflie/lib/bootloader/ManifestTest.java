package se.bitcraze.crazyflie.lib.bootloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;

@SuppressWarnings("java:S106")
@Category(OfflineTests.class)
public class ManifestTest {

    private static final String STM32 = "stm32";
    private static final String CFLIE2_BIN = "cflie2.bin";

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

    @Test
    public void testReadManifest() throws IOException {
        Manifest readManifest = Bootloader.readManifest(new File("src/test/manifest.json"));

        assertNotNull(readManifest);
        System.out.println("Version: " + readManifest.getVersion());
        for (String name : readManifest.getFiles().keySet()) {
            System.out.println("Name: " + name);
            System.out.println(readManifest.getFiles().get(name).toString());
        }

        assertEquals(1, readManifest.getVersion());
        assertTrue(readManifest.getFiles().containsKey(CFLIE2_BIN));
        FirmwareDetails firmwareDetails1 = readManifest.getFiles().get(CFLIE2_BIN);
        assertEquals("cf2", firmwareDetails1.getPlatform());
        assertEquals(STM32, firmwareDetails1.getTarget());
        assertEquals("fw", firmwareDetails1.getType());

        assertTrue(readManifest.getFiles().containsKey("cf2_nrf_1.1.bin"));
        FirmwareDetails firmwareDetails2 = readManifest.getFiles().get("cf2_nrf_1.1.bin");
        assertEquals("cf2", firmwareDetails2.getPlatform());
        assertEquals("nrf51", firmwareDetails2.getTarget());
        assertEquals("fw", firmwareDetails2.getType());
    }

    @Test
    public void writeManifest() throws IOException {
        Manifest manifest = new Manifest();
        manifest.setVersion(1);
        Map<String, FirmwareDetails> map = new HashMap<>();
        FirmwareDetails firmwareDetails = new FirmwareDetails("cf2", STM32, "fw", "2015.01.11", "release-repo");
        map.put(CFLIE2_BIN, firmwareDetails);
        manifest.setFiles(map);

        String testFileName = "manifestTest.json";
        Bootloader.writeManifest(testFileName, manifest);

        File testFile = new File(testFileName);
        assertTrue("Test file should exist.", testFile.exists());
        assertTrue("Test file should have a length > 0.", testFile.length() > 0);

//        System.out.println("Version: " + readManifest.getVersion());
//        for (String name : readManifest.getFiles().keySet()) {
//            System.out.println("Name: " + name);
//            System.out.println(readManifest.getFiles().get(name).toString());
//        }

    }

}
