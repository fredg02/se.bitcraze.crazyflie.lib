package se.bitcraze.crazyflie.lib;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.bootloader.Cloader;
import se.bitcraze.crazyflie.lib.bootloader.Target.TargetTypes;
import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.crazyradio.RadioAck;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket.Header;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.log.Logg;
import se.bitcraze.crazyflie.lib.param.Param;
import se.bitcraze.crazyflie.lib.toc.Toc;
import se.bitcraze.crazyflie.lib.toc.TocCache;
import se.bitcraze.crazyflie.lib.toc.TocElement;
import se.bitcraze.crazyflie.lib.toc.TocFetcher;

/**
 * Mocks replies from CF2
 *
 */
public class MockRadio extends Crazyradio {

    final Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private boolean hasFwScan = true;

    private final static int TOC_CRC_LOGG = 0xF14AC355;
    private final static int TOC_CRC_PARAM = 0x3E16885D;
    
    private int consoleTextCounter = 0;

    private final byte[] defaultData = new byte[] {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};
    private Toc mParamToc;

    private static List<String> consoleByteStrings = new ArrayList<String>();
    static {
        consoleByteStrings.add("01 00 53 59 53 3A 20 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 00");
        consoleByteStrings.add("01 00 2D 2D 2D 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        consoleByteStrings.add("01 00 53 59 53 3A 20 43 72 61 7A 79 66 6C 69 65 20 32 2E 30 20 69 73 20 75 70 20 61 6E 64 20 72 00");
        consoleByteStrings.add("01 00 75 6E 6E 69 6E 67 21 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        consoleByteStrings.add("01 00 53 59 53 3A 20 42 75 69 6C 64 20 33 38 3A 33 65 34 30 38 61 65 35 34 65 31 65 20 28 32 30 00");
        consoleByteStrings.add("01 00 31 37 2E 30 36 2D 33 38 29 20 43 4C 45 41 4E 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        consoleByteStrings.add("41 00 53 59 53 3A 20 49 20 61 6D 20 30 78 33 37 33 33 33 36 33 32 33 31 33 33 34 37 30 33 32 46 00");
        consoleByteStrings.add("01 00 30 30 33 44 20 61 6E 64 20 49 20 68 61 76 65 20 31 30 32 34 4B 42 20 6F 66 20 66 6C 61 73 00");
        consoleByteStrings.add("01 00 68 21 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        consoleByteStrings.add("01 00 43 46 47 42 4C 4B 3A 20 76 31 2C 20 76 65 72 69 66 69 63 61 74 69 6F 6E 20 5B 4F 4B 5D 0A 00");
        consoleByteStrings.add("01 00 44 45 43 4B 5F 44 52 49 56 45 52 53 3A 20 46 6F 75 6E 64 20 31 32 20 64 72 69 76 65 72 73 00");
        consoleByteStrings.add("01 00 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        consoleByteStrings.add("01 00 44 45 43 4B 5F 49 4E 46 4F 3A 20 46 6F 75 6E 64 20 31 20 64 65 63 6B 20 6D 65 6D 6F 72 79 00");
        consoleByteStrings.add("11 00 2E 0A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        consoleByteStrings.add("01 00 44 45 43 4B 5F 43 4F 52 45 3A 20 31 20 64 65 63 6B 20 65 6E 75 6D 65 72 61 74 65 64 0A 00 00");
        consoleByteStrings.add("01 00 4D 6F 74 69 6F 6E 20 63 68 69 70 20 69 73 3A 20 30 78 34 39 0A 00 00 00 00 00 00 00 00 00 00");
        consoleByteStrings.add("01 00 73 69 20 70 69 68 63 20 6E 6F 69 74 6F 4D 3A 20 30 78 42 36 0A 00 00 00 00 00 00 00 00 00 00");
        consoleByteStrings.add("01 00 4D 50 55 39 32 35 30 20 49 32 43 20 63 6F 6E 6E 65 63 74 69 6F 6E 20 5B 4F 4B 5D 2E 0A 00 00");
        consoleByteStrings.add("01 00 41 4B 38 39 36 33 20 49 32 43 20 63 6F 6E 6E 65 63 74 69 6F 6E 20 5B 4F 4B 5D 2E 0A 00 00 00");
        consoleByteStrings.add("01 00 4C 50 53 32 35 48 20 49 32 43 20 63 6F 6E 6E 65 63 74 69 6F 6E 20 5B 4F 4B 5D 2E 0A 00 00 00");
        consoleByteStrings.add("01 00 45 53 54 49 4D 41 54 4F 52 3A 20 55 73 69 6E 67 20 65 73 74 69 6D 61 74 6F 72 20 32 0A 00 00");
        consoleByteStrings.add("01 00 45 45 50 52 4F 4D 3A 20 49 32 43 20 63 6F 6E 6E 65 63 74 69 6F 6E 20 5B 4F 4B 5D 2E 0A 00 00");
        consoleByteStrings.add("01 00 41 4B 38 39 36 33 3A 20 53 65 6C 66 20 74 65 73 74 20 5B 4F 4B 5D 2E 0A 00 00 00 00 00 00 00");
        consoleByteStrings.add("01 00 44 45 43 4B 5F 43 4F 52 45 3A 20 44 65 63 6B 20 30 20 74 65 73 74 20 5B 4F 4B 5D 2E 0A 00 00");
        consoleByteStrings.add("01 00 53 59 53 3A 20 46 72 65 65 20 68 65 61 70 3A 20 31 36 31 30 34 20 62 79 74 65 73 0A 00 00 00");
    }

    public MockRadio() {
        TocCache tocCache = new TocCache(new File("src/test"));
        mParamToc = tocCache.fetch(TOC_CRC_PARAM, CrtpPort.PARAMETERS);
    }

    @Override
    public void disconnect() {
        mLogger.debug("MockRadio disconnect()");
    }

    @Override
    public boolean hasFwScan() {
        mLogger.debug("MockRadio hasFwScan");
        return hasFwScan;
    }

    /**
     * Method for testing
     * 
     * @param hasFwScan
     */
    public void setHasFwScan(boolean hasFwScan) {
        this.hasFwScan = hasFwScan;
    }

    @Override
    public List<Integer> scanChannels(int start, int stop) {
        mLogger.debug("MockRadio scanChannels from " + start + " to " + stop);
        //TODO: do I need to return at least one integer?
        return new ArrayList<Integer>();
    }

    @Override
    protected List<Integer> firmwareScan(int start, int stop) {
        mLogger.debug("MockRadio firmwareScan from " + start + " to " + stop);
        //TODO: do I need to return at least one integer?
        return new ArrayList<Integer>();
    }

    private byte[] byteStringToPacket (String byteString) {
        int packetSize = 33;
//        if (byteString.length() > packetSize) {
//            System.err.println("Length of byte string is bigger than " + packetSize);
//            return new byte[0];
//        }
        ByteBuffer packetByteBuffer = ByteBuffer.allocate(packetSize);
        String[] split = byteString.split(" ");
        int l = split.length;
//        packetByteBuffer.put((byte) 1);
//        packetByteBuffer.put((byte) 0);
        for (int i=0; i < l; i++) {
            packetByteBuffer.put(Integer.decode("0x" + split[i]).byteValue());
        }
        return packetByteBuffer.array();
    }

    @Override
    public RadioAck sendPacket(byte[] dataOut) {
        byte headerByte = dataOut[0];
        if (headerByte != (byte) 0xFF) {
            mLogger.debug("MockDriver sendPacket:    " + Utilities.getHexString(dataOut));
        }
        
        Header header = new Header(headerByte);
        CrtpPort port = header.getPort();
        
        byte[] payload = Arrays.copyOfRange(dataOut, 1, dataOut.length);

        RadioAck ackIn = null;
        byte[] data = new byte[33];
        
        // fill data with default data (empty packet)
        data = defaultData;

        if (headerByte == (byte) 0xFF && dataOut.length == 1) { // Console (answers to null packets)
            if (consoleTextCounter < consoleByteStrings.size()-1) {
                data = byteStringToPacket(consoleByteStrings.get(consoleTextCounter));
                consoleTextCounter++;
            }
        } else if (headerByte == (byte) 0xFF) {                 // Bootloader
            data = bootloader(payload);
        } else {
            if (port == CrtpPort.LOGGING) {                     // Logging
                data = logging(header, payload);
            } else if (port == CrtpPort.PARAMETERS) {           // Parameters
                data = parameters(header, payload);
            }

        }
        
        if (headerByte != (byte) 0xFF) {
            mLogger.debug("MockDriver receivePacket: " + Utilities.getHexString(data));
        }
                
        // if data is not None:
        ackIn = new RadioAck();
        if (data[0] != 0) {
            ackIn.setAck((data[0] & 0x01) != 0);
            ackIn.setPowerDet((data[0] & 0x02) != 0);
            ackIn.setRetry(data[0] >> 4);
            ackIn.setData(Arrays.copyOfRange(data, 1, data.length));
        } else {
            ackIn.setRetry(mArc);
        }
        return ackIn;
    }

    private byte[] toc(Header header, byte[] payload) {
        byte[] data = defaultData;

        int command = payload[0];
        CrtpPort port = header.getPort();
        
        int tocCRC;
        if (port == CrtpPort.LOGGING) {
            tocCRC = TOC_CRC_LOGG;
        } else {
            // Parameters
            tocCRC = TOC_CRC_PARAM;
        }
        byte[] tocCRCBytes = new byte[4];
        ByteBuffer.wrap(tocCRCBytes).order(ByteOrder.BIG_ENDIAN).putInt(tocCRC);
        
//        tocCRC = 0xBDB60123;
        
        int tocLength = 255; //??
        int maxLogBlocks = 16; //??
        int maxLogVariables = 128; //??

        if (port == CrtpPort.LOGGING) {
            if (command == (byte) TocFetcher.CMD_TOC_INFO) {
                mLogger.debug("Logging - Command: CMD_GET_INFO/CMD_TOC_INFO");
                data = new byte[]{1, header.getByte(), (byte) command, (byte) tocLength, tocCRCBytes[0], tocCRCBytes[1], tocCRCBytes[2], tocCRCBytes[3], (byte) maxLogBlocks, (byte) maxLogVariables};
            } else if (command == (byte) TocFetcher.CMD_TOC_ELEMENT) {
                int toc_item_index = payload[1];
                mLogger.debug("Logging - Command: CMD_GET_ITEM/CMD_TOC_ELEMENT) - item index: " + toc_item_index);
                //TODO: add toc item data
                int type = 0;
                String group = "group";
                byte[] groupBytes = new byte[8];
                ByteBuffer.wrap(groupBytes).order(ByteOrder.BIG_ENDIAN).put(group.getBytes());
                String variable = "variable";
                byte[] variableBytes = new byte[8];
                ByteBuffer.wrap(variableBytes).order(ByteOrder.BIG_ENDIAN).put(variable.getBytes());

                data = new byte[]{1, header.getByte(), (byte) command, (byte) toc_item_index, (byte) type, groupBytes[0], groupBytes[1], groupBytes[2], groupBytes[3], groupBytes[4], groupBytes[5], variableBytes[0], variableBytes[1], variableBytes[2], variableBytes[3], variableBytes[4], variableBytes[5]};
            }
        } else if (port == CrtpPort.PARAMETERS) {
            if (command == (byte) TocFetcher.CMD_TOC_INFO) {
                mLogger.debug("Parameters - Command: CMD_TOC_INFO");
                data = new byte[]{1, header.getByte(), (byte) command, (byte) tocLength, tocCRCBytes[0], tocCRCBytes[1], tocCRCBytes[2], tocCRCBytes[3]};
            } else if (command == (byte) TocFetcher.CMD_TOC_ELEMENT) {
                int toc_item_index = payload[1];
                mLogger.debug("Parameters - Command: CMD_GET_ITEM/CMD_TOC_ELEMENT) - item index: " + toc_item_index);
                //TODO: add toc item data
                int type = 0;
                String group = "group";
                byte[] groupBytes = new byte[8];
                ByteBuffer.wrap(groupBytes).order(ByteOrder.BIG_ENDIAN).put(group.getBytes());
                String variable = "variable";
                byte[] variableBytes = new byte[12];
                ByteBuffer.wrap(variableBytes).order(ByteOrder.BIG_ENDIAN).put(variable.getBytes());
                    
                data = new byte[]{1, header.getByte(), (byte) command, (byte) toc_item_index, (byte) type, groupBytes[0], groupBytes[1], groupBytes[2], groupBytes[3], groupBytes[4], groupBytes[5], variableBytes[0], variableBytes[1], variableBytes[2], variableBytes[3], variableBytes[4], variableBytes[5], variableBytes[6], variableBytes[7], variableBytes[8]};
          }
        }
        return data;
    }

    /**
     * Bootloader
     * 
     * @param packet
     * @return
     */
    protected byte[] bootloader(byte[] payload) {
        byte[] data = defaultData;

        if (payload[1] == (byte) Cloader.GET_INFO) {
            if (payload[0] == (byte) TargetTypes.STM32) {
                mLogger.debug("Bootloader - Command: GET_INFO - STM32 - CF2");
                /*
                    OUT:    -1,-1,16,
                    IN:     1,-1,-1,16,0,4,10,0,0,4,16,0,-89,4,48,106,79,-33,34,94,-1,-27,20,-112,16,0,0,0,0,0,0,0,0,
                 */
                data = new byte[] {-1,-1,16,0,4,10,0,0,4,16,0,-89,4,48,106,79,-33,34,94,-1,-27,20,-112,16,0,0,0,0,0,0,0,0};
            } else if (payload[0] == (byte) TargetTypes.NRF51) {
                mLogger.debug("Bootloader - Command: GET_INFO - NRF51");
                /*
                    OUT:    -1,-2,16,
                    IN:     1,-1,-2,16,0,4,1,0,-24,0,88,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
                 */
                data = new byte[] {-1,-2,16,0,4,1,0,-24,0,88,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};
            }
        } else if (payload[1] == Cloader.WRITE_FLASH) {
            mLogger.debug("Bootloader - Command: WRITE_FLASH");
            // Reply from CF is always the same
            /*
                OUT:   -1,-1,24,0,0,10,0,10,0,
                IN:    113,-1,-1,24,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
             */

            /*
                OUT:   -2,24,0,0,88,0,1,0, //NRF51
             */
            //TODO: add error messages
            //TODO: adapt better support for NRF51
            data = new byte[] {-1, payload[0],24,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};
        } else if (payload[0] == (byte) TargetTypes.NRF51 && payload[1] == (byte) 0xF0) {
            mLogger.debug("Bootloader - Command: Reset to firmware - CF2");
            /*
                Reset to firmware (CF2)
                OUT: -2,-1,1,2,4,5,6,7,8,9,10,11,12,
                OUT: -2,-16,1,
             */
        } else if (payload[0] == (byte) TargetTypes.STM32 && payload[1] == (byte) 0xF0) {
            mLogger.debug("Bootloader - Command: Reset to firmware - CF1");
            /*
                Reset to firmware (CF1)
                OUT: -1,-1,-1,1,2,4,5,6,7,8,9,10,11,12,
                OUT: -1,-1,-16,1,2,4,5,6,7,8,9,10,11,12,
             */
        } else if (payload[1] == Cloader.GET_MAPPING) {
            mLogger.debug("Bootloader - Command: GET_MAPPING - CF2");
            // Update mapping
            /*
                OUT:   -1,-1,18,
                IN:     1,-1,-1,18,4,16,1,64,7,-128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
             */
            data = new byte[] {-1,-1,18,4,16,1,64,7,-128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            //TODO: improve!?
        }
        return data;
    }

    private byte[] logging(Header header, byte[] payload) {
        byte[] data = defaultData;
        int channel = header.getChannel();
        int command = payload[0];

        if (channel == Logg.CHAN_TOC) {
            data = toc(header, payload);
        } else if (channel == Logg.CHAN_SETTINGS) {
            int expectedReplyCommand = command; //reply is the same as payload[1]
            int id = 0;
            if (command == Logg.CMD_RESET_LOGGING) {
                mLogger.debug("Logging - Command: CMD_RESET_LOGGING");
            } else {
                id = payload[1];
                if (command == Logg.CMD_START_LOGGING) {
                    mLogger.debug("Logging - Command: CMD_START_LOGGING - id: " + id);
                } else if (command == Logg.CMD_STOP_LOGGING) {
                    mLogger.debug("Logging - Command: CMD_STOP_LOGGING - id: " + id);
                } else if (command == Logg.CMD_CREATE_LOGCONFIG) {
                    mLogger.debug("Logging - Command: CMD_CREATE_LOGCONFIG - id: " + id);
                } else if (command == Logg.CMD_DELETE_LOGCONFIG) {
                    mLogger.debug("Logging - Command: CMD_DELETE_LOGCONFIG - id: " + id);
                }
            }
            data = new byte[]{1, header.getByte(), (byte) expectedReplyCommand, (byte) id, 0};
        }
        return data;
    }

    private int paramTest_testParamSet_newValue = -1;
    
    private byte[] parameters(Header header, byte[] payload) {
        byte[] data = defaultData;
        int channel = header.getChannel();
        int id = payload[0]; // & 0x00ff; ?
        TocElement tocElement = mParamToc.getElementById(id);

        if (channel == Param.TOC_CHANNEL) {
            data = toc(header, payload);
        } else if (channel == Param.READ_CHANNEL) {
            mLogger.debug("Parameters - Command: READ_PARAM - id: " + id);
            int value = 42;
            if ("sound.freq".equals(tocElement.getCompleteName())) {  // ParamTest.testParamSet
                value = (paramTest_testParamSet_newValue != -1) ? paramTest_testParamSet_newValue : 4000; // default value of sound.freq is 4000
            }
            data = createParamPacketData(header, id, value);
        } else if (channel == Param.WRITE_CHANNEL) {
            int value = (int) getParamValue(id, payload);
            mLogger.debug("Parameters - Command: WRITE_PARAM - id: " + id + " - value: " + value);
            if ("sound.freq".equals(tocElement.getCompleteName())) {  // ParamTest.testParamSet
                paramTest_testParamSet_newValue = value;
            }
            data = createParamPacketData(header, id, value);
        }
        return data;
    }

    private Number getParamValue(int id, byte[] payload) {
        TocElement tocElement = mParamToc.getElementById(id);
        ByteBuffer byteBuffer = ByteBuffer.wrap(payload, 1, payload.length-1);
        Number number = tocElement.getCtype().parse(byteBuffer);
        return number;
    }
    
    private byte[] createParamPacketData(Header header, int id, int value) {
        //TODO: can we just create a CrtpPacket (the first byte (1) is probably missing)?
        TocElement tocElement = mParamToc.getElementById(id);
        byte[] parse = tocElement.getCtype().parse(value);
        ByteBuffer bb = ByteBuffer.allocate(parse.length+3);
        bb.put((byte) 1);
        bb.put(header.getByte());
        bb.put((byte) tocElement.getIdent());
        bb.put(parse);
        return bb.array();
        
    }
    
    @Override
    protected void sendVendorSetup(int request, int value, int index, byte[] data) {
        //TODO: decode request
        mLogger.debug("MockRadio sendVendorSetup request: " + request + ", value: " + value + ", index: " + index + ", data: " + data.toString());
    }

    @Override
    public float getVersion() {
        return 0.42f;
    }

    @Override
    public String getSerialNumber() {
        return "DEADBEEF";
    }
}
