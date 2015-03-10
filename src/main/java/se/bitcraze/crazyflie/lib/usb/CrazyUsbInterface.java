package se.bitcraze.crazyflie.lib.usb;

import java.util.List;

import javax.usb.UsbDevice;

/**
 * Abstract USB interface to be independent of different implementations (eg. Java/Android)
 *
 */
public interface CrazyUsbInterface {

    /**
     * Release UsbInterface
     *
     */
    public void releaseInterface();

    /**
     * Returns the state of the USB connection
     *
     * @return true if USB device is connected, else false
     */
    public boolean isUsbConnected();

    /**
     * Send control data
     *
     * @param requestType
     * @param request
     * @param value
     * @param index
     * @param data
     * @return
     */
    public int sendControlTransfer(int requestType, int request, int value, int index, byte[] data);

    /**
     * Sends bulk data
     *
     * @param data
     * @param receiveData
     * @return
     */
    public int sendBulkTransfer(byte[] data, byte[] receiveData);

    /**
     * Returns a list of CrazyRadio devices currently connected to the computer
     *
     * @return
     */
    public List<UsbDevice> findDevices();

    /**
     * Returns the firmware version of the USB device
     *
     * @return firmware version
     */
    public float getFirmwareVersion();

    /**
     * Returns the serial number of the USB device
     *
     * @return serial number
     */
    public String getSerialNumber();

}