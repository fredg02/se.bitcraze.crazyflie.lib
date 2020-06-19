/**
 *    ||          ____  _ __
 * +------+      / __ )(_) /_______________ _____  ___
 * | 0xBC |     / __  / / __/ ___/ ___/ __ `/_  / / _ \
 * +------+    / /_/ / / /_/ /__/ /  / /_/ / / /_/  __/
 *  ||  ||    /_____/_/\__/\___/_/   \__,_/ /___/\___/
 *
 * Copyright (C) 2013 Bitcraze AB
 *
 * Crazyflie Nano Quadcopter Client
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package se.bitcraze.crazyflie.lib.crazyradio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.usb.CrazyUsbInterface;

/**
 * Used for communication with the Crazyradio USB dongle
 *
 */
public class Crazyradio {

    protected final Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    // USB parameters
    public static final int CRADIO_VID = 0x1915; //Vendor ID
    public static final int CRADIO_PID = 0x7777; //Product ID

    // Dongle configuration requests
    // See http://wiki.bitcraze.se/projects:crazyradio:protocol for documentation
    private static final int SET_RADIO_CHANNEL = 0x01;
    private static final int SET_RADIO_ADDRESS = 0x02;
    private static final int SET_DATA_RATE = 0x03;
    private static final int SET_RADIO_POWER = 0x04;
    private static final int SET_RADIO_ARD = 0x05;
    private static final int SET_RADIO_ARC = 0x06;
    //private static final int ACK_ENABLE = 0x10;
    private static final int SET_CONT_CARRIER = 0x20;
    private static final int SCAN_CHANNELS = 0x21;
    //private static final int LAUNCH_BOOTLOADER = 0xFF;

    // configuration constants
    public static final int DR_250KPS = 0;
    public static final int DR_1MPS = 1;
    public static final int DR_2MPS = 2;

    public static final int P_M18DBM = 0;
    public static final int P_M12DBM = 1;
    public static final int P_M6DBM = 2;
    public static final int P_0DBM = 3;

    private CrazyUsbInterface mUsbInterface;
    protected int mArc;
    private float mVersion; // Crazyradio firmware version
    private String mSerialNumber; // Crazyradio serial number

    protected static final byte[] NULL_PACKET = new byte[] { (byte) 0xff };

    protected Crazyradio () {
    }

    /**
     * Create object and scan for USB dongle if no device is supplied
     *
     * @param usbInterface
     */
    public Crazyradio(CrazyUsbInterface usbInterface) {
        this.mUsbInterface = usbInterface;
        try {
            this.mUsbInterface.initDevice(CRADIO_VID, CRADIO_PID);
        } catch (SecurityException | IOException e) {
            mLogger.error(e.getMessage());
            return;
        }

        /*
        def __init__(self, device=None, devid=0):
            if device is None:
                try:
                    device = _find_devices()[devid]
                except Exception:
                    raise Exception("Cannot find a Crazyradio Dongle")

            self.dev = device
        */

        this.mVersion = mUsbInterface.getFirmwareVersion();

        if (this.mVersion < 0.4) {
            this.mLogger.warn("You should update to Crazyradio firmware V0.4+");
        }

        this.mSerialNumber = mUsbInterface.getSerialNumber();

        // Reset the dongle to power up settings
        this.mLogger.debug("Resetting dongle to power up settings...");
        setDatarate(DR_2MPS);
        setChannel(2);
        this.mArc = -1;
        if (mVersion >= 0.4) {
            setContinuousCarrier(false);
            // self.set_address((0xE7,) * 5)
            setAddress(new byte[] {(byte) 0xE7, (byte) 0xE7, (byte) 0xE7, (byte) 0xE7, (byte) 0xE7});
            setPower(P_0DBM);
            setArc(3);
            setArdBytes(32);
        }

    }

    public void disconnect() {
        mLogger.debug("disconnect()");
        if(mUsbInterface != null) {
            mUsbInterface.releaseInterface();
        }
    }

    /* ### Dongle configuration ### */

    /**
     * Set the radio channel to be used.
     *
     * @param channel new channel. Must be in range 0-125.
     */
    public void setChannel(int channel) {
        if (channel < 0 || channel > 125) {
            throw new IllegalArgumentException("Channel must be an integer value between 0 and 125");
        }
        sendVendorSetup(SET_RADIO_CHANNEL, channel, 0, null);
    }

    /**
     * Set the radio address to be used.
     * The same address must be configured in the receiver for the communication to work.
     *
     * @param address new address with a length of 5 byte.
     * @throws IllegalArgumentException if the length of the address doesn't equal 5 bytes
     */
    public void setAddress(byte[] address) {
        if (address.length != 5) {
            throw new IllegalArgumentException("Radio address must be 5 bytes long");
        }
        sendVendorSetup(SET_RADIO_ADDRESS, 0, 0, address);
    }

    /**
     * Set the radio data rate to be used.
     *
     * @param datarate new data rate. Possible values are in range 0-2.
     */
    public void setDatarate(int datarate) {
        if (datarate < 0 || datarate > 2) {
            throw new IllegalArgumentException("Data rate must be an int value between 0 and 2");
        }
        sendVendorSetup(SET_DATA_RATE, datarate, 0, null);
    }

    /**
     * Set the radio power to be used
     *
     * @param power new radio power
     */
    public void setPower(int power) {
        //TODO: add argument check
        sendVendorSetup(SET_RADIO_POWER, power, 0, null);
    }

    /**
     * Set the ACK retry count for radio communication
     *
     * Set how often the radio will retry a transfer if the ACK has not been received.
     *
     * @param arc number of retries.
     * @throws IllegalArgumentException if the number of retries is not in range 0-15.
     */
    public void setArc(int arc) {
        if (arc < 0 || arc > 15) {
            throw new IllegalArgumentException("Count must be in range 0-15");
        }
        sendVendorSetup(SET_RADIO_ARC, arc, 0, null);
        this.mArc = arc;
    }

    /**
     * Set the ACK retry delay for radio communication
     *
     * Configure the time the radio waits for the acknowledge.
     *
     * @param us microseconds to wait. Will be rounded to the closest possible value supported by the radio.
     */
    public void setArdTime(int us) {
        /*
        # Auto Retransmit Delay:
        # 0000 - Wait 250uS
        # 0001 - Wait 500uS
        # 0010 - Wait 750uS
        # ........
        # 1111 - Wait 4000uS
        */
        // Round down, to value representing a multiple of 250uS
        int t = (int) Math.round(us / 250.0) - 1;
        if (t < 0) {
            t = 0;
        } else if (t > 0xF) {
            t = 0xF;
        }
        sendVendorSetup(SET_RADIO_ARD, t, 0, null);
    }

    /**
     * Set the length of the ACK payload.
     *
     * @param nbytes number of bytes in the payload.
     * @throws IllegalArgumentException if the payload length is not in range 0-32.
     */
    public void setArdBytes(int nbytes) {
        if (nbytes < 0 || nbytes > 32) {
            throw new IllegalArgumentException("Payload length must be in range 0-32");
        }
        sendVendorSetup(SET_RADIO_ARD, 0x80 | nbytes, 0, null);
    }

    /**
     * Set the continuous carrier mode. When enabled the radio chip provides a
     * test mode in which a continuous non-modulated sine wave is emitted. When
     * this mode is activated the radio dongle does not transmit any packets.
     *
     * @param active <code>true</code> to enable the continuous carrier mode
     */
    public void setContinuousCarrier(boolean active) {
        sendVendorSetup(SET_CONT_CARRIER, (active ? 1 : 0), 0, null);
    }

    protected boolean hasFwScan() {
        /*
          #return self.version >= 0.5
          return mUsbInterface.getFirmwareVersion() > 0.5;
          # FIXME: Mitigation for Crazyradio firmware bug #9
        */
        return false;
    }

    /**
     * Scan all channels between 0 and 125
     *
     * @return list of channels
     */
    public List<Integer> scanChannels() {
        return scanChannels(0, 125);
    }

    public List<Integer> scanChannels(int start, int stop) {
        List<Integer> result = new ArrayList<>();

        if (mUsbInterface != null && mUsbInterface.isUsbConnected()) {
            if (hasFwScan()) {
                result.addAll(firmwareScan(start, stop));
            } else {
                result = slowScan(start, stop);
            }
        } else {
            mLogger.warn("Crazyradio not attached.");
        }
        return result;
    }


    /**
     * Slow PC-driven scan
     *
     * @param start
     * @param stop
     * @return list of channels
     */
    private List<Integer> slowScan(int start, int stop) {
        mLogger.debug("Slow scan...");
        List<Integer> result = new ArrayList<>();
        // for i in range(start, stop + 1):
        for (int channel = start; channel <= stop; channel++) {
            if(scanSelected(channel, NULL_PACKET)) {
                mLogger.debug("Found channel: {}", channel);
                result.add(channel);
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                mLogger.error("InterruptedException: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        return result;
    }

    /*
    def scan_selected(self, selected, packet):
        result = ()
        for s in selected:
            self.set_channel(s["channel"])
            self.set_data_rate(s["datarate"])
            status = self.send_packet(packet)
            if status and status.ack:
                result = result + (s,)

        return result
    */

    public boolean scanSelected(int channel, int datarate, byte[] packet) {
        setDatarate(datarate);
        return scanSelected(channel, packet);
    }

    private boolean scanSelected(int channel, byte[] packet) {
        setChannel(channel);
        RadioAck status = sendPacket(packet);
        return (status != null && status.isAck());
    }


    /* ### Data transfers ### */

    protected List<Integer> firmwareScan(int start, int stop) {
        mLogger.debug("Fast scan...");
        List<Integer> result = new ArrayList<>();
        final byte[] rdata = new byte[64];
        mUsbInterface.sendControlTransfer(0x40, SCAN_CHANNELS, start, stop, NULL_PACKET);
        final int nfound = mUsbInterface.sendControlTransfer(0xc0, SCAN_CHANNELS, 0, 0, rdata);
        if (nfound != 64) {
            for (int i = 0; i < nfound; i++) {
                result.add((int) rdata[i]);
                mLogger.debug("Found channel: {}", rdata[i]);
            }
        }
        return result;
    }

    /**
     * Send a packet and receive the ack from the radio dongle.
     * The ack contains information about the packet transmission
     * and a data payload if the ack packet contained any
     *
     * @param dataOut bytes to send
     */
    public RadioAck sendPacket(byte[] dataOut) {
        RadioAck ackIn = null;
        byte[] data = new byte[33]; // 33?

        if (mUsbInterface == null || !mUsbInterface.isUsbConnected()) {
            return null;
        }
        mUsbInterface.sendBulkTransfer(dataOut, data);

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

    protected void sendVendorSetup(int request, int value, int index, byte[] data) {
        // usb.TYPE_VENDOR = 64 <=> 0x40
        int usbTypeVendor = 0x40;
        mUsbInterface.sendControlTransfer(usbTypeVendor, request, value, index, data);
    }

    public float getVersion() {
        return this.mVersion;
    }

    public String getSerialNumber() {
        return this.mSerialNumber;
    }

}
