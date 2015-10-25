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

package se.bitcraze.crazyflie.lib;

import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;

/**
 * Dummy driver for testing purposes
 *
 */
public class DummyDriver extends CrtpDriver {

    @Override
    public void connect(ConnectionData connectionData) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendPacket(CrtpPacket packet) {
        // TODO Auto-generated method stub
    }

    @Override
    public CrtpPacket receivePacket(int wait) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean scanSelected(int channel, int datarate, byte[] packet) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void startSendReceiveThread() {
        // TODO Auto-generated method stub
    }

    @Override
    public void stopSendReceiveThread() {
        // TODO Auto-generated method stub
    }

}
