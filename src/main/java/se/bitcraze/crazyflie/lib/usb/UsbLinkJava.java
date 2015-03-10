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

import se.bitcraze.crazyflie.lib.crazyradio.Crazyradio;


/**
 * TODO: While multiple Crazyradios can be found with findDevices() only the first Crazyradio
 * is initialized and used for communication. This should be fixed.
 *
 */
public class UsbLinkJava implements CrazyUsbInterface {

    final static Logger mLogger = LoggerFactory.getLogger("UsbLinkJava");

    //USB Timeout is set in javax.usb.properties

    private UsbDevice mUsbDevice;
    private UsbInterface mIntf;
    private UsbEndpoint mEpIn;
    private UsbEndpoint mEpOut;

    private UsbHub mRootHub;

    public UsbLinkJava() {
        try {
            if(mIntf == null || !mIntf.isClaimed()){
                initDevice();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UsbException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize the USB device. Determines endpoints and prepares communication.
     *
     * @throws IOException if the device cannot be opened
     * @throws UsbException
     * @throws SecurityException
     */
    private void initDevice() throws IOException, SecurityException, UsbException {
        UsbServices services = UsbHostManager.getUsbServices();
        mRootHub = services.getRootUsbHub();
        List<UsbDevice> usbDevices = findDevices();
        if (usbDevices.isEmpty()) {
            mLogger.info("Crazyradio not found.");
            return;
        }
        this.mUsbDevice = usbDevices.get(0);
        // TODO: Only gets the first Crazyradio that is found
        if (mUsbDevice == null) {
            mLogger.info("Crazyradio not found.");
            return;
        }

        mLogger.debug("setDevice " + this.mUsbDevice);

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

            mIntf.claim();
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

                mLogger.debug("sendControlTransfer,  requestType: {}, request: {}, value: {}, index: {}, data: {}", requestType, request, value, index, getByteString(data));

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
                e.printStackTrace();
            }
        }
        return returnCode;
    }

    //TODO: better method name?
    private int sendBulkTransfer(UsbEndpoint usbEndpoint, byte[] data) throws UsbException{
        int returnCode = -1;

        if(UsbConst.ENDPOINT_DIRECTION_OUT == usbEndpoint.getDirection()){
            mLogger.debug("sendBulkTransfer - direction: OUT,  byteString: {}", getByteString(data));
        }

        UsbPipe usbPipe = usbEndpoint.getUsbPipe();
        if (usbPipe != null) {
            if(!usbPipe.isOpen()){
                usbPipe.open();
            }
            if (usbPipe.isOpen()) {
                UsbIrp usbIrp = usbPipe.createUsbIrp();
                usbIrp.setData(data);
                int dataLength = (data == null) ? 0 : data.length;
                usbIrp.setLength(dataLength);
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
        	//TODO: show type of packet in debug log
            mLogger.debug("sendBulkTransfer - direction: IN,  byteString: {}", getByteString(data));
        }

        return returnCode;
    }

    /**
     * Returns byte array as comma separated string
     * (for debugging purposes)
     *
     * @param data
     * @return
     */
    public static String getByteString(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            sb.append(b);
            sb.append(",");
        }
        String byteString = sb.toString();
        return byteString;
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
            System.out.println("DCP submission failed : " + uE.getMessage());
            return false;
        }
    }

    public List<UsbDevice> findDevices() {
        return findUsbDevices(mRootHub, (short) Crazyradio.CRADIO_VID, (short) Crazyradio.CRADIO_PID);
    }

    @SuppressWarnings("unchecked")
    public static List<UsbDevice> findUsbDevices(UsbHub hub, short vendorId, short productId) {
        List<UsbDevice> usbDeviceList = new ArrayList<UsbDevice>();
        if (hub != null) {
            for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
                UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
                if (desc.idVendor() == vendorId && desc.idProduct() == productId){
                    mLogger.debug("Found Crazyradio!");
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
