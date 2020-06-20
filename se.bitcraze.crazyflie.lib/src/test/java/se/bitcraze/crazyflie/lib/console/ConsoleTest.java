package se.bitcraze.crazyflie.lib.console;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import se.bitcraze.crazyflie.lib.OfflineTests;

@Category(OfflineTests.class)
@SuppressWarnings("java:S106")
public class ConsoleTest {

    private static List<String> byteStrings = new ArrayList<>();
    static {
        byteStrings.add("01 00 53 59 53 3A 20 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 00");
        byteStrings.add("01 00 2D 2D 2D 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        byteStrings.add("01 00 53 59 53 3A 20 43 72 61 7A 79 66 6C 69 65 20 32 2E 30 20 69 73 20 75 70 20 61 6E 64 20 72 00");
        byteStrings.add("01 00 75 6E 6E 69 6E 67 21 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        byteStrings.add("01 00 53 59 53 3A 20 42 75 69 6C 64 20 33 38 3A 33 65 34 30 38 61 65 35 34 65 31 65 20 28 32 30 00");
        byteStrings.add("01 00 31 37 2E 30 36 2D 33 38 29 20 43 4C 45 41 4E 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        byteStrings.add("41 00 53 59 53 3A 20 49 20 61 6D 20 30 78 33 37 33 33 33 36 33 32 33 31 33 33 34 37 30 33 32 46 00");
        byteStrings.add("01 00 30 30 33 44 20 61 6E 64 20 49 20 68 61 76 65 20 31 30 32 34 4B 42 20 6F 66 20 66 6C 61 73 00");
        byteStrings.add("01 00 68 21 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        byteStrings.add("01 00 43 46 47 42 4C 4B 3A 20 76 31 2C 20 76 65 72 69 66 69 63 61 74 69 6F 6E 20 5B 4F 4B 5D 0A 00");
        byteStrings.add("01 00 44 45 43 4B 5F 44 52 49 56 45 52 53 3A 20 46 6F 75 6E 64 20 31 32 20 64 72 69 76 65 72 73 00");
        byteStrings.add("01 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        byteStrings.add("01 00 44 45 43 4B 5F 49 4E 46 4F 3A 20 46 6F 75 6E 64 20 31 20 64 65 63 6B 20 6D 65 6D 6F 72 79 00");
        byteStrings.add("11 00 2E 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        byteStrings.add("01 00 44 45 43 4B 5F 43 4F 52 45 3A 20 31 20 64 65 63 6B 20 65 6E 75 6D 65 72 61 74 65 64 0A 00 00");
        byteStrings.add("01 00 4D 6F 74 69 6F 6E 20 63 68 69 70 20 69 73 3A 20 30 78 34 39 0A 00 00 00 00 00 00 00 00 00 00");
        byteStrings.add("01 00 73 69 20 70 69 68 63 20 6E 6F 69 74 6F 4D 3A 20 30 78 42 36 0A 00 00 00 00 00 00 00 00 00 00");
        byteStrings.add("01 00 4D 50 55 39 32 35 30 20 49 32 43 20 63 6F 6E 6E 65 63 74 69 6F 6E 20 5B 4F 4B 5D 2E 0A 00 00");
        byteStrings.add("01 00 41 4B 38 39 36 33 20 49 32 43 20 63 6F 6E 6E 65 63 74 69 6F 6E 20 5B 4F 4B 5D 2E 0A 00 00 00");
        byteStrings.add("01 00 4C 50 53 32 35 48 20 49 32 43 20 63 6F 6E 6E 65 63 74 69 6F 6E 20 5B 4F 4B 5D 2E 0A 00 00 00");
        byteStrings.add("01 00 45 53 54 49 4D 41 54 4F 52 3A 20 55 73 69 6E 67 20 65 73 74 69 6D 61 74 6F 72 20 32 0A 00 00");
        byteStrings.add("01 00 45 45 50 52 4F 4D 3A 20 49 32 43 20 63 6F 6E 6E 65 63 74 69 6F 6E 20 5B 4F 4B 5D 2E 0A 00 00");
        byteStrings.add("01 00 41 4B 38 39 36 33 3A 20 53 65 6C 66 20 74 65 73 74 20 5B 4F 4B 5D 2E 0A 00 00 00 00 00 00 00");
        byteStrings.add("01 00 44 45 43 4B 5F 43 4F 52 45 3A 20 44 65 63 6B 20 30 20 74 65 73 74 20 5B 4F 4B 5D 2E 0A 00 00");
        byteStrings.add("01 00 53 59 53 3A 20 46 72 65 65 20 68 65 61 70 3A 20 31 36 31 30 34 20 62 79 74 65 73 0A 00 00 00");
    }

    @Test
    public void consoleTest() {
        // create packet in a ByteBuffer
        int packetSize = 33;
        ByteBuffer packetByteBuffer = ByteBuffer.allocate((packetSize-2) * byteStrings.size());
        for (String single : byteStrings) {
            String[] split = single.split(" ");
            int l = split.length;
            for (int i=2; i < l; i++) {
                //print out raw values in a line
//                System.out.print(split[i]);
                packetByteBuffer.put(Integer.decode("0x" + split[i]).byteValue());
            }
//            System.out.println();
        }
//        System.out.println("PacketByteBuffer capacity: " + packetByteBuffer.capacity());

        ByteBuffer tempDecodeBuffer = ByteBuffer.allocate(packetByteBuffer.capacity());
        int n;
        for (n=0; n < packetByteBuffer.capacity(); n++) {
            //read in one byte from packetByteBuffer
            byte b = packetByteBuffer.get(n);
            if (b == 0) {
                continue;
            } else {
                tempDecodeBuffer.put(b);
            }
        }
//        System.out.println("found 0A: " + n + " bb2.position: " + tempDecodeBuffer.position());
        System.out.println(new String(tempDecodeBuffer.array()));
//        System.out.println("n: " + n);
        tempDecodeBuffer.clear();
        tempDecodeBuffer.rewind();
    }
}
