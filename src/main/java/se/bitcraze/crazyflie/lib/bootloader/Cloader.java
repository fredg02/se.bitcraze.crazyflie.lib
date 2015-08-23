package se.bitcraze.crazyflie.lib.bootloader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.bootloader.Target.TargetTypes;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;
import se.bitcraze.crazyflie.lib.crazyradio.RadioAck;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket.Header;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;

/**
 * Crazyflie radio bootloader for flashing firmware.
 *
 * Bootloader utility for the Crazyflie
 *
 */
public class Cloader {

    final Logger mLogger = LoggerFactory.getLogger("Cloader");

    private CrtpDriver mDriver;
    private List<ConnectionData> mAvailableBootConnections = new ArrayList<ConnectionData>();

    private Map<Integer, Target> mTargets = new HashMap<Integer, Target>();

    /**
     * Init the communication class by starting to communicate with the link given.
     * clink is the link address used after resetting to the bootloader.
     *
     * The device is actually considered to be in firmware mode.
     */
    // def __init__(self, link, info_cb=None, in_boot_cb=None):
    public Cloader(CrtpDriver driver) {
        this.mDriver = driver;

        //self._available_boot_uri = ("radio://0/110/2M", "radio://0/0/2M")
        mAvailableBootConnections.add(new ConnectionData(110, Crazyradio.DR_2MPS));
        mAvailableBootConnections.add(new ConnectionData(0, Crazyradio.DR_2MPS));
    }

    /**
     * Close the link
     */
    public void close() {
        if (this.mDriver != null) {
            this.mDriver.disconnect();
        }
    }

    /**
     * Scans for bootloader with the predefined channel/datarate combinations<br/>
     * Timeout is 10 seconds.
     *
     * @return always the first available bootloader connection
     */
    public ConnectionData scanForBootloader() {
        long startTime = System.currentTimeMillis();
        List<ConnectionData> resultList = new ArrayList<ConnectionData>();
        while (resultList.size() == 0 && (System.currentTimeMillis() - startTime) < 10000) {
            for (ConnectionData cd : mAvailableBootConnections) {
                if(this.mDriver.scanSelected(cd.getChannel(), cd.getDataRate(), new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF})) {
                    resultList.add(cd);
                }
            }
        }
//        mDriver.disconnect();

        if (resultList.size() > 0) {
            return resultList.get(0);
        }
        return null;
    }

    public boolean resetToBootloader(int targetId) {
        int retryCounter = 5;

        Header header = new Header((byte) 0xFF);
        //pk.data = (target_id, 0xFF)
        CrtpPacket pk = new CrtpPacket(header.getByte(), new byte[]{(byte) targetId, (byte) 0xFF});
        this.mDriver.sendPacket(pk);

        CrtpPacket replyPk = this.mDriver.receivePacket(1);
        //while ((not pk or pk.header != 0xFF or struct.unpack("<BB", pk.data[0:2]) != (target_id, 0xFF)) and retry_counter >= 0 ):
        while (replyPk == null || replyPk.getHeader().getByte() != 0xFF || replyPk.getPayload()[0] != (byte) targetId || replyPk.getPayload()[1] != (byte) 0xFF && retryCounter >= 0) {
            replyPk = this.mDriver.receivePacket(1);
            retryCounter -= 1;
        }

        if (replyPk != null) {

            //TODO: new_address = (0xb1, ) + struct.unpack("<BBBB", pk.data[2:6][::-1])
            byte[] newAddress = null;

            //pk.data = (target_id, 0xF0, 0x00)
            CrtpPacket pk2 = new CrtpPacket(header.getByte(), new byte[]{(byte) targetId, (byte) 0xF0, (byte) 0x00});
            this.mDriver.sendPacket(pk2);

            //TODO: addr = int(struct.pack("B"*5, *new_address).encode('hex'), 16)

            //TODO: time.sleep(0.2)
            this.mDriver.disconnect();
            //TODO: time.sleep(0.2)
            //TODO: self.link = cflib.crtp.get_link_driver("radio://0/0/2M/{}".format(addr))
            return true;
        }
        //TODO: fix dead code warning
        return false;
    }

    /**
     * Reset to the bootloader
     *
     * The parameter cpuid shall correspond to the device to reset.
     *
     * Return true if the reset has been done and the contact with the
     * bootloader is established.
     */
    //TODO: cpu_id = target id??
    public boolean resetToBootloader1(int cpuId) {
        /*
         * Send an echo request and wait for the answer
         * Mainly aim to bypass a bug of the crazyflie firmware that prevents
         * reset before normal CRTP communication
         */
        //pk = CRTPPacket()
        //pk.port = CRTPPort.LINKCTRL
        Header header = new Header(0, CrtpPort.LINKCTRL);
        //pk.data = (1, 2, 3) + cpu_id
        CrtpPacket pk = new CrtpPacket(header.getByte(), new byte[]{1, 2, 3, (byte) cpuId});
        this.mDriver.sendPacket(pk);

        // Wait for reply
        CrtpPacket replyPk = null;
        while(true) {
            replyPk = this.mDriver.receivePacket(2);
            if (replyPk == null) {
                return false;
            }
            if (replyPk.getHeader().getPort() == CrtpPort.LINKCTRL) {
                break;
            }
        }

        // Send the reset to bootloader request
        Header header2= new Header((byte) 0xFF);
        //pk.data = (0xFF, 0xFE) + cpu_id
        CrtpPacket resetPk = new CrtpPacket(header2.getByte(), new byte[]{(byte) 0xFF, (byte) 0xFE, (byte) cpuId});
        this.mDriver.sendPacket(resetPk);

        //Wait to ack the reset ...
        CrtpPacket replyPk2 = null;
        while(true) {
            replyPk2 = this.mDriver.receivePacket(2);
            if (replyPk2 == null) {
                return false;
            }
            // if pk.port == 0xFF and tuple(pk.data) == (0xFF, 0xFE) + cpu_id:
            if(replyPk2.getHeader().getPort() == CrtpPort.ALL &&
                    replyPk2.getPayload()[0] == (byte) 0xFF &&
                    replyPk2.getPayload()[1] == (byte) 0xFE &&
                    replyPk2.getPayload()[2] == (byte) cpuId) {
                CrtpPacket resetPk2 = new CrtpPacket(header2.getByte(), new byte[]{(byte) 0xFF, (byte) 0xF0, (byte) cpuId});
                this.mDriver.sendPacket(replyPk2);
                break;
            }
        }

        //time.sleep(0.1)
        this.mDriver.disconnect();
        //TODO: self.link = cflib.crtp.get_link_driver(self.clink_address)
        //time.sleep(0.1)

        return updateInfo(cpuId); //cpuId = targetId?
    }

    /**
     * The parameter cpuid shall correspond to the device to reset.
     *
     * @param targetId
     * @return true if the reset has been done
     */
    public boolean resetToFirmware(int targetId) {
        /*
         * The fake CPU ID is legacy from the Crazyflie 1.0
         * In order to reset the CPU ID had to be sent, but this
         * was removed before launching it. But the length check is
         * still in the bootloader. So to work around this bug so
         * some extra data needs to be sent.
         * fake_cpu_id = (1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12)
         */

        // Send the reset to bootloader request
        //pk.set_header(0xFF, 0xFF)
        Header header= new Header((byte) 0xFF);
        //pk.data = (target_id, 0xFF) + fake_cpu_id
        CrtpPacket pk = new CrtpPacket(header.getByte(), new byte[]{(byte) targetId, (byte) 0xFF, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        this.mDriver.sendPacket(pk);

        // Wait to ack the reset ...
        CrtpPacket replyPk = null;
        while(true) {
            replyPk = this.mDriver.receivePacket(2);

            if (replyPk != null && replyPk.getHeaderByte() == (byte) 0xFF) {
                // struct.unpack("<BB", pk.data[0:2]) == (target_id, 0xFF))
                byte data1 = replyPk.getPayload()[0];
                byte data2 = replyPk.getPayload()[1];

                if (data1 == (byte) targetId && data2 == (byte) 0xFF) {
                    // Difference in CF1 and CF2 (CPU ID)
                    byte[] data = null;
                    if (targetId == TargetTypes.NRF51) { // CF2
                        // pk.data = (target_id, 0xF0, 0x01)
                        data = new byte[] {(byte) targetId, (byte) 0xF0, (byte) 0x01};
                    } else { // CF1
                        // pk.data = (target_id, 0xF0) + fake_cpu_id
                        data = new byte[] {(byte) targetId, (byte) 0xF0, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12};
                    }
                    CrtpPacket resetPk = new CrtpPacket(header.getByte(), data);
                    this.mDriver.sendPacket(resetPk);
                    break;
                }
            }
        }
        //time.sleep(0.1)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void openBootloaderConnection(ConnectionData connectionData) {
//        if (this.mDriver != null) {
//            this.mDriver.disconnect();
//        }
        if (connectionData != null) {
            this.mDriver.connect(connectionData);
        } else {
            // self.link = cflib.crtp.get_link_driver(self.clink_address)
        }
    }

    /**
     * Try to get a connection with the bootloader by requesting info
     * 5 times. This let roughly 10 seconds to boot the copter ...
     */
    //def check_link_and_get_info(self, target_id=0xFF):
    public boolean checkLinkAndGetInfo(int targetId) {
        for (int i = 0; i < 5; i++) {
            if (updateInfo(targetId)) {

                /*
                if self._in_boot_cb:
                    self._in_boot_cb.call(True, self.targets[target_id].protocol_version)
                if self._info_cb:
                    self._info_cb.call(self.targets[target_id])
                */
//                if (this.mProtocolVersion != 1) {
//                    return true;
//                }
                // Set radio link to a random address
                /*
                addr = [0xbc] + map(lambda x: random.randint(0, 255), range(4))
                return self._set_address(addr)
                */
                return true;
            }

            //TODO: is this necessary?
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Change copter radio address.
     * This function works only with crazyradio CRTP link.
     */
    public boolean setAddress(byte[] newAddress) {
        if (newAddress.length != 5) {
            mLogger.error("Radio address should be 5 bytes long");
            return false;
        }

        mLogger.debug("Setting bootloader radio address to " + getHexString(newAddress));

        // TODO: self.link.pause()

        for (int i = 0; i < 10; i++) {
            mLogger.debug("Trying to set new radio address");

            //TODO: deal with other driver implementations
            Crazyradio crazyRadio = ((RadioDriver) this.mDriver).getRadio();
            //TODO: self.link.cradio.set_address((0xE7,) * 5)
            crazyRadio.setAddress(new byte[]{(byte) 0xE7, (byte) 0xE7, (byte) 0xE7, (byte) 0xE7, (byte) 0xE7});

            //TODO: is there a more elegant way to do this?
            //pkdata = (0xFF, 0xFF, 0x11) + tuple(new_address)
            byte[] pkData = new byte[newAddress.length + 3];
            pkData[0] = (byte) 0xFF;
            pkData[1] = (byte) 0xFF;
            pkData[2] = (byte) 0x11;;
            System.arraycopy(newAddress, 0, pkData, 3, newAddress.length);
            crazyRadio.sendPacket(pkData);

            //self.link.cradio.set_address(tuple(new_address))
            crazyRadio.setAddress(newAddress);


            //if self.link.cradio.send_packet((0xff,)).ack:
            RadioAck ack = crazyRadio.sendPacket(new byte[] {(byte) 0xFF});
            if (ack != null) {
                //logging.info("Bootloader set to radio address" " {}".format(new_address))

                mLogger.info("Bootloader set to radio address " + getHexString(newAddress));;
                //TODO: this.mDriver.restart()
                return true;
            }

        }
        //TODO: this.mDriver.restart();
        return false;
    }

    public Target requestInfoUpdate(int targetId) {
        if (!this.mTargets.containsKey(targetId)) {
            updateInfo(targetId);
        }
        /*
        if self._info_cb:
            self._info_cb.call(self.targets[target_id])
        */
        return this.mTargets.get(targetId);
    }

    /**
     * Call the command getInfo and fill up the information received in the fields of the object
     */
    public boolean updateInfo(int targetId) {

        // Call getInfo ...
        // pk.set_header(0xFF, 0xFF)
        Header header = new Header((byte) 0xFF);
        // pk.data = (target_id, 0x10)
        CrtpPacket pk = new CrtpPacket(header.getByte(), new byte[]{(byte) targetId, (byte) 0x10});
        this.mDriver.sendPacket(pk);

        // Wait for the answer
        CrtpPacket replyPk = this.mDriver.receivePacket(2);

        if (replyPk != null && replyPk.getHeaderByte() == (byte) 0xFF) {
            // struct.unpack("<BB", pk.data[0:2]) == (target_id, 0x10))
            byte data1 = replyPk.getPayload()[0];
            byte data2 = replyPk.getPayload()[1];

            if (data1 == (byte) targetId && data2 == (byte) 0x10) {
                Target target = new Target(targetId);
                target.parseData(replyPk.getPayload());

                if (!this.mTargets.containsKey(targetId)) {
                    this.mTargets.put(targetId, target);
                } else {
                    //TODO: update existing entry
                }

                if (target.getProtocolVersion() == (byte) 0x10 && targetId == TargetTypes.STM32) {
                    updateMapping(targetId);
                }
                return true;
            } else {
                System.err.println("Payload problem");
            }
        }
        return false;
    }

    public void updateMapping(int targetId) {
        Header header = new Header((byte) 0xFF);
        CrtpPacket pk = new CrtpPacket(header.getByte(), new byte[]{(byte) targetId, (byte) 0x12});
        this.mDriver.sendPacket(pk);

        CrtpPacket replyPk = this.mDriver.receivePacket(2);
        if (replyPk != null && replyPk.getHeaderByte() == (byte) 0xFF && replyPk.getPayload()[0] == (byte) targetId && replyPk.getPayload()[1] == (byte) 0x12){

            //TODO: m = pk.datat[2:]
            int dataLength = replyPk.getPayload().length-2;
            byte[] m = new byte[dataLength];
            System.arraycopy(replyPk, 2, m, 0, dataLength);

            if (m.length % 2 != 0){
                //raise Exception("Malformed flash mapping packet")
                mLogger.error("Malformed flash mapping packet");
                return;
            }

            /*
            self.mapping = []
            page = 0
            for i in range(len(m)/2):
                for j in range(m[2*i]):
                    self.mapping.append(page)
                    page += m[(2*i)+1]
             */
            List<Integer> mapping = new ArrayList<Integer>();
            int page = 0;
            for (int i = 0; i < m.length/2; i++) {
                for (int j = 0; j < m[2*i]; j++) {
                    mapping.add(page);
                    page += m[(2*i)+1];
                }
            }

            System.out.println("Mapping: " + Arrays.toString(mapping.toArray()));
        }

    }

    /**
     * Upload data into a buffer on the Crazyflie
     */
    public void uploadBuffer(int targetId, int page, int address, byte[] buff) {
        int count = 0;
        Header header = new Header((byte) 0xFF);
        //pk.data = struct.pack("=BBHH", target_id, 0x14, page, address)
        ByteBuffer bb = ByteBuffer.allocate(6+buff.length).order(ByteOrder.LITTLE_ENDIAN);
        bb.put((byte) targetId);
        bb.put((byte) 0x14);
        bb.putChar((char) page);
        bb.putChar((char) address);

        CrtpPacket pk = null;

        for (int i = 0; i < buff.length; i++) {
            bb.put(buff[i]);

            count++;

            if (count > 24) {
                pk = new CrtpPacket(header.getByte(), bb.array());
                this.mDriver.sendPacket(pk);
                count = 0;

                //pk.data = struct.pack("=BBHH", target_id, 0x14, page, i + address + 1)
                ByteBuffer bb2 = ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN);
                bb2.put((byte) targetId);
                bb2.put((byte) 0x14);
                bb2.putChar((char) page);
                bb2.putChar((char) (i + address + 1));

                CrtpPacket pk2 = new CrtpPacket(header.getByte(), bb2.array());
                this.mDriver.sendPacket(pk2);
            }
        }
        this.mDriver.sendPacket(pk);
    }

    /**
     * Read back a flash page from the Crazyflie and return it
     */
    //def read_flash(self, addr=0xFF, page=0x00):
    public byte[] readFlash(int addr, int page) {
        ByteBuffer buff = null;
        Header header = new Header((byte) 0xFF);

        Target target = this.mTargets.get(addr);
        if (target != null) {
            int pageSize = target.getPageSize();
            buff = ByteBuffer.allocate(pageSize + 1);

            for (int i = 0; i < Math.ceil(pageSize / 25.0); i++) {
                CrtpPacket pk = null;
                CrtpPacket replyPk = null;
                int retryCounter = 5;

                //struct.unpack("<BB", pk.data[0:2]) != (addr, 0x1C))
                byte data1 = -1;
                byte data2 = -1;

                while (retryCounter >= 0) {

                    //TODO ByteOrder?
                    ByteBuffer bb = ByteBuffer.allocate(6).order(ByteOrder.LITTLE_ENDIAN);
                    bb.put((byte) addr);
                    bb.put((byte) 0x1C);
                    bb.putChar((char) page);
                    bb.putChar((char) (i*25));
                    pk = new CrtpPacket(header.getByte(), bb.array());

                    this.mDriver.sendPacket(pk);
                    //System.out.println("ByteString send: " + getHexString(pk.getPayload()) + " " + UsbLinkJava.getByteString(pk.getPayload()));

                    //TODO: why is this different than in Python?
                    //does it have something to do with the queue size??
                    //yes, the queue is filled with empty packets
                    //how can this be avoided?
                    while(replyPk == null || replyPk.getHeaderByte() != (byte) 0xFF || replyPk.getPayload()[1] != (byte) 0x1C) {
                        replyPk = this.mDriver.receivePacket(10);
                    }

                    if (replyPk != null) {
                        //System.out.println("ByteString rece: " + UsbLinkJava.getByteString(replyPk.getPayload()));

                        data1 = replyPk.getPayload()[0];
                        data2 = replyPk.getPayload()[1];

                        if (replyPk.getHeaderByte() == (byte) 0xFF && (data1 == (byte) addr && data2 == (byte) 0x1C)) {
                            break;
                        }
                    }
                    retryCounter--;
                }
                if (retryCounter < 0) {
                    System.out.println("Returning null...");
                    return null;
                } else {
                    buff.put(replyPk.getPayload(), 6, replyPk.getPayload().length - 6);
                }
            }

        }
        //return buff[0:page_size]  # For some reason we get one byte extra here...
        //-> because of the ceil function?
        return buff.array();
    }

    /**
     * Initiate flashing of data in the buffer to flash.
     */
    public boolean writeFlash(int addr, int pageBuffer, int targetPage, int pageCount) {
        /*
        #print "Write page", flashPage
        #print "Writing page [%d] and [%d] forward" % (flashPage, nPage)
        */
        CrtpPacket pk = null;
        Header header = new Header((byte) 0xFF);
        int retryCounter = 5;
        //#print "Flasing to 0x{:X}".format(addr)

        while (pk == null || pk.getHeaderByte() != (byte) 0xFF || pk.getPayload()[0] != (byte) addr || pk.getPayload()[1] != (byte) 0x18 && retryCounter >= 0) {
            //pk.data = struct.pack("<BBHHH", addr, 0x18, page_buffer, target_page, page_count)
            ByteBuffer bb = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            bb.put((byte) addr);
            bb.put((byte) 0x18);
            bb.putChar((char) pageBuffer);
            bb.putChar((char) targetPage);
            bb.putChar((char) pageCount);
            pk = new CrtpPacket(header.getByte(), bb.array());
            this.mDriver.sendPacket(pk);

            //TODO: use two different variables
            pk = this.mDriver.receivePacket(1);

            retryCounter--;
        }

        if (retryCounter < 0) {
            //self.error_code = -1
            return false;
        }
        //self.error_code = ord(pk.data[3])

        //return ord(pk.data[2]) == 1
        return true;
    }

    //decode_cpu_id has not been implemented, because it's not used anywhere

    public List<Target> getTargets() {
        return (List<Target>) mTargets.values();
    }

    public static String getHexString(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (byte b : array) {
            sb.append(String.format("0x%02X", b));
            sb.append(" ");
        }
        return sb.toString();
    }

}