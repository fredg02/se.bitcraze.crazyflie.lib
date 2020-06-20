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

import se.bitcraze.crazyflie.lib.Utilities;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;


/**
 * TODO: While multiple Crazyradios can be found with findDevices() only the first Crazyradio
 * is initialized and used for communication. This should be fixed.
 *
 */
public class UsbLinkJavaDebug extends UsbLinkJava {

    private static final String UNKNOWN = "UNKNOWN";

    public UsbLinkJavaDebug() {
    }

    @Override
    protected void debugBulkTransfer(String direction, byte[] data) {
        //TODO: show type of packet in debug log
        if (FILTER_OUT_NULL_AND_ACK_PACKETS &&
           (("OUT".equalsIgnoreCase(direction) && "FF ".equalsIgnoreCase(Utilities.getHexString(data))) ||
           ("IN".equalsIgnoreCase(direction) && Utilities.getHexString(data).startsWith("01 00 00")))) {
           return;
        }

        CrtpPacket.Header header = null;
        int command = -1;
        if ("IN".equalsIgnoreCase(direction)) {
            header = new CrtpPacket.Header(data[1]);
            command = data[2];
        } else {
            header = new CrtpPacket.Header(data[0]);
            command = data[1];
        }
        String portName = header.getPort().name();
        int channel = header.getChannel();

        String channelName = UNKNOWN;
        String commandName = UNKNOWN;
        if (header.getPort() == CrtpPort.LOGGING) {
            switch (channel) {
            case 0:
                channelName = "TOC access";
                break;
            case 1:
                channelName = "LOG control";
                commandName = getLogControlCommandName(command);
                break;
            case 2:
                channelName = "Log data";
                break;
            default:
                channelName = UNKNOWN;
                commandName = UNKNOWN;
                break;
            }
        }else if (header.getPort() == CrtpPort.PARAMETERS) {
            channelName = getParametersChannelName(channel);
        }

        //Filter out "ALL" packets
        if (header.getPort() == CrtpPort.ALL) {
            return;
        }

        direction = String.format("%3s", direction);
        portName = String.format("%11s", portName);
        channelName = String.format("%11s", channelName);
        commandName = String.format("%7s", commandName);
        mLogger.debug("bulkTransfer - <->: {}, port: {}, channel: {}, commandName: {}, bytes: {}", direction, portName, channelName, commandName, Utilities.getHexString(data));
    }

    private String getLogControlCommandName(int command) {
        String commandName = "";
        switch (command) {
        case 0:
            commandName = "CREATE";
            break;
        case 1:
            commandName = "APPEND";
            break;
        case 2:
            commandName = "DELETE";
            break;
        case 3:
            commandName = "START";
            break;
        case 4:
            commandName = "STOP";
            break;
        case 5:
            commandName = "RESET";
            break;
        default:
            commandName = UNKNOWN;
            break;
        }
        return commandName;
    }

    private String getParametersChannelName(int channel) {
        String channelName = "";
        switch (channel) {
        case 0:
            channelName = "TOC access";
            break;
        case 1:
            channelName = "PARAM read";
            break;
        case 2:
            channelName = "PARAM write";
            break;
        case 3:
            channelName = "Misc";
            break;
        default:
            channelName = "Unknown";
            break;
        }
        return channelName;
    }
}
