/**
 *    ||          ____  _ __
 * +------+      / __ )(_) /_______________ _____  ___
 * | 0xBC |     / __  / / __/ ___/ ___/ __ `/_  / / _ \
 * +------+    / /_/ / / /_/ /__/ /  / /_/ / / /_/  __/
 *  ||  ||    /_____/_/\__/\___/_/   \__,_/ /___/\___/
 *
 * Copyright (C) 2015 Bitcraze AB
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

package se.bitcraze.crazyflie.lib.usb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbIrp;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.Utilities;


/**
 * TODO: While multiple Crazyradios can be found with findDevices() only the first Crazyradio
 * is initialized and used for communication. This should be fixed.
 *
 */
public class UsbLinkJava implements CrazyUsbInterface {

    final static Logger mLogger = LoggerFactory.getLogger("UsbLinkJava");

    private static final boolean FILTER_OUT_NULL_AND_ACK_PACKETS = true;

    //USB Timeout is set in javax.usb.properties

    private UsbDevice mUsbDevice;
    private UsbInterface mIntf;
    private UsbEndpoint mEpIn;
    private UsbEndpoint mEpOut;

    private UsbHub mRootHub;

    public UsbLinkJava() {
    }

    /**
     * Initialize the USB device. Determines endpoints and prepares communication.
     *
     * @param vid
     * @param pid
     * @throws IOException if the device cannot be opened
     * @throws SecurityException
     */
    public void initDevice(int vid, int pid) throws IOException, SecurityException {
        if(mIntf != null && mIntf.isClaimed()){
            mLogger.warn("USB device is already initialized or claimed.");
            return;
        }
        try {
            UsbServices services = UsbHostManager.getUsbServices();
            mRootHub = services.getRootUsbHub();
        } catch (UsbException e) {
            // convert to IOException to make Crazyradio independent of USB implementation
            throw new IOException(e.getMessage());
        }
        List<UsbDevice> usbDevices = findUsbDevices(mRootHub, (short) vid, (short) pid);
        if (usbDevices.isEmpty() || usbDevices.get(0) == null) {
            mLogger.warn("USB device not found. (VID: {}, PID: {})", vid, pid);
            return;
        }
        // TODO: Only gets the first USB device that is found
        this.mUsbDevice = usbDevices.get(0);

        mLogger.debug("setDevice {}", this.mUsbDevice);

        UsbConfiguration activeUsbConfiguration = this.mUsbDevice.getActiveUsbConfiguration();

        // find interface
        if (activeUsbConfiguration.getUsbInterfaces().size() != 1) {
            mLogger.error("Could not find interface");
            return;
        }
        mIntf = (UsbInterface) activeUsbConfiguration.getUsbInterfaces().get(0);

        if(mIntf != null){
            // device should have two endpoints
            if (mIntf.getUsbEndpoints().size() != 2) {
            	mLogger.error("Could not find endpoints");
                return;
            }
            // endpoints should be of type bulk
            UsbEndpoint ep = (UsbEndpoint) mIntf.getUsbEndpoints().get(0);
            if (ep.getType() != UsbConst.ENDPOINT_TYPE_BULK) {
            	mLogger.error("Endpoint is not of type bulk");
                return;
            }
            // check endpoint direction
            if (ep.getDirection() == UsbConst.ENDPOINT_DIRECTION_IN) {
                mEpIn = (UsbEndpoint) mIntf.getUsbEndpoints().get(0);
                mEpOut = (UsbEndpoint) mIntf.getUsbEndpoints().get(1);
            } else {
                mEpIn = (UsbEndpoint) mIntf.getUsbEndpoints().get(1);
                mEpOut = (UsbEndpoint) mIntf.getUsbEndpoints().get(0);
            }

            try {
                mIntf.claim();
            } catch (UsbException e) {
                // convert to IOException to make Crazyradio independent of USB implementation
                throw new IOException(e.getMessage());
            }
            if (mIntf.isClaimed()) {
            	mLogger.debug("UsbInterface claim SUCCESS");
            }else{
            	mLogger.error("UsbInterface claim ERROR");
            }
        }else{
        	mLogger.error("UsbInterface is NULL");
            throw new IOException("Could not open usb connection");
        }
    }

    /* (non-Javadoc)
     * @see CrazyUsbInterface#releaseInterface()
     */
    public void releaseInterface(){
        if(mIntf != null && mIntf.isClaimed()){
            try {
                mIntf.release();
                mIntf = null;
                mLogger.debug("UsbInterface released");
            } catch (UsbException e) {
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see CrazyUsbInterface#isUsbConnected()
     */
    public boolean isUsbConnected(){
        return mIntf != null && mIntf.isClaimed();
    }

    /* (non-Javadoc)
     * @see CrazyUsbInterface#sendControlTransfer(int, int, int, int, byte[])
     */
    public int sendControlTransfer(int requestType, int request, int value, int index, byte[] data) {
        int returnCode = -1;
        if(mUsbDevice != null){
            try {
                UsbControlIrp usbControlIrp = mUsbDevice.createUsbControlIrp((byte) requestType, (byte) request, (short) value, (short) index);
                if(data == null){
                    data = new byte[0];
                }
                usbControlIrp.setData(data);
                int dataLength = (data == null) ? 0 : data.length;
                usbControlIrp.setLength(dataLength);
                debugControlTransfer((byte) requestType, (byte) request, (byte) value, (byte) index, data);
                if(sendUsbControlIrp(mUsbDevice, usbControlIrp)){
                    returnCode = usbControlIrp.getActualLength();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (UsbDisconnectedException e) {
                e.printStackTrace();
            }
        }
        return returnCode;
    }

    private void debugControlTransfer(byte requestType, byte request, byte value, byte index, byte[] data) {
        mLogger.debug("sendControlTransfer, requestType: {}, request: {}, value: {}, index: {}, data: {}", Utilities.getHexString(requestType), Utilities.getHexString(request), Utilities.getHexString(value), Utilities.getHexString(index), Utilities.getHexString(data));
    }

    /* (non-Javadoc)
     * @see CrazyUsbInterface#sendBulkTransfer(byte[], byte[])
     */
    public int sendBulkTransfer(byte[] data, byte[] receiveData){
        int returnCode = -1;
        if(mUsbDevice != null){
            try {
                sendBulkTransfer(mEpOut, data);
                returnCode = sendBulkTransfer(mEpIn, receiveData);
            } catch (UsbException e) {
                mLogger.error("sendBulkTransfer failed: " + e.getMessage());
            }
        }
        return returnCode;
    }

    public void bulkWrite(byte[] data) {
        if(mUsbDevice != null){
            try {
                sendBulkTransfer(mEpOut, data);
            } catch (UsbException e) {
                mLogger.error("bulkWrite failed: " + e.getMessage());
            }
        } else {
            mLogger.error("bulkWrite failed because mUsbDevice was null");
        }
    }

    public byte[] bulkRead() {
        int returnCode = -1;
        byte[] data = new byte[33];
        if(mUsbDevice != null){
            try {
                returnCode = sendBulkTransfer(mEpIn, data);
                mLogger.debug("bulkRead: return code = {}", returnCode);
            } catch (UsbException e) {
                mLogger.error("bulkRead failed: " + e.getMessage());
            }
        }
        return data;
    }

    //TODO: better method name?
    private int sendBulkTransfer(UsbEndpoint usbEndpoint, byte[] data) throws UsbException{
        int returnCode = -1;

        if(UsbConst.ENDPOINT_DIRECTION_OUT == usbEndpoint.getDirection()){
            debugBulkTransfer("OUT", data);
        }

        UsbPipe usbPipe = usbEndpoint.getUsbPipe();
        if (usbPipe != null) {
            if(!usbPipe.isOpen()){
                usbPipe.open();
            }
            if (usbPipe.isOpen()) {
                UsbIrp usbIrp = usbPipe.createUsbIrp();
                usbIrp.setData(data);
                //TODO: data length does not need to be set explicitly
//                int dataLength = (data == null) ? 0 : data.length;
//                usbIrp.setLength(dataLength);
                usbPipe.syncSubmit(usbIrp);
                if (!usbIrp.isUsbException()) {
                    returnCode = usbIrp.getActualLength();
                } else {
                    // throw usbControlIrp.getUsbException();
                    usbIrp.getUsbException().printStackTrace();
                }
            } else {
            	mLogger.error("usbPipe open ERROR");
            }
        } else {
        	mLogger.error("usbPipe is NULL");
        }

        if(UsbConst.ENDPOINT_DIRECTION_IN == usbEndpoint.getDirection()){
            debugBulkTransfer("IN", data);
        }

        return returnCode;
    }

    private void debugBulkTransfer(String direction, byte[] data) {
        //TODO: show type of packet in debug log
        if (FILTER_OUT_NULL_AND_ACK_PACKETS &&
           (("OUT".equalsIgnoreCase(direction) && "FF ".equalsIgnoreCase(Utilities.getHexString(data))) ||
           ("IN".equalsIgnoreCase(direction) && Utilities.getHexString(data).startsWith("01 00 00")))) {
           return;
        }
        mLogger.debug("sendBulkTransfer - direction: {}, byteString: {}", direction, Utilities.getHexString(data));
    }

    /**
     * Send the UsbControlIrp to the UsbDevice on the DCP.
     *
     * @param usbDevice
     *            The UsbDevice.
     * @param usbControlIrp
     *            The UsbControlIrp.
     * @return If the submission was successful.
     */
    public static boolean sendUsbControlIrp(UsbDevice usbDevice, UsbControlIrp usbControlIrp) {
        try {
            /*
             * This will block until the submission is complete.
             * Note that submissions (except interrupt and bulk in-direction)
             * will not block indefinitely, they will complete or fail within a finite
             * amount of time. See MouseDriver.HidMouseRunnable for more details.
             */
            usbDevice.syncSubmit(usbControlIrp);
            return true;
        } catch (UsbException uE) {
            mLogger.error("DCP submission failed : " + uE.getMessage());
            return false;
        }
    }

    public List<UsbDevice> findDevices(int vid, int pid) {
        return findUsbDevices(mRootHub, (short) vid, (short) pid);
    }

    @SuppressWarnings("unchecked")
    public static List<UsbDevice> findUsbDevices(UsbHub hub, short vendorId, short productId) {
        List<UsbDevice> usbDeviceList = new ArrayList<UsbDevice>();
        if (hub != null) {
            for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
                UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
                if (desc.idVendor() == vendorId && desc.idProduct() == productId){
                    mLogger.debug("Found USB device!");
                    usbDeviceList.add(device);
                }
                if (device.isUsbHub()) {
                    usbDeviceList.addAll(findUsbDevices((UsbHub) device, vendorId, productId));
                }
            }
        }
        return usbDeviceList;
    }

    /* (non-Javadoc)
     * @see se.bitcraze.crazyflie.lib.IUsbLink#getFirmwareVersion()
     */
    public float getFirmwareVersion() {
        return getFirmwareVersion(mUsbDevice);
    }

    public static float getFirmwareVersion(UsbDevice usbDevice) {
        if (usbDevice == null) {
            return Float.valueOf("0.0");
        }
        return Float.valueOf("0." + Integer.toHexString(usbDevice.getUsbDeviceDescriptor().bcdDevice()));
    }

    public String getSerialNumber() {
        return getSerialNumber(mUsbDevice);
    }

    public static String getSerialNumber(UsbDevice usbDevice) {
        if (usbDevice == null) {
            return "N/A";
        }
        try {
            return usbDevice.getSerialNumberString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "N/A";
        } catch (UsbDisconnectedException e) {
            e.printStackTrace();
            return "N/A";
        } catch (UsbException e) {
            e.printStackTrace();
            return "N/A";
        }
    }

}
