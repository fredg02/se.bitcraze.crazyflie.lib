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

import se.bitcraze.crazyflie.lib.crazyflie.ConnectionListener;

/**
 * An abstract adapter class for receiving connection events.
 * This class exists as convenience for creating listener objects in test.
 */
public abstract class TestConnectionAdapter implements ConnectionListener {

    public void connectionRequested(String connectionInfo) {
        System.out.println("CONNECTION REQUESTED: " + connectionInfo);
    }

    public void connected(String connectionInfo) {
        System.out.println("CONNECTED: " + connectionInfo);
    }

    public void setupFinished(String connectionInfo) {
        System.out.println("SETUP FINISHED: " + connectionInfo);
    }

    public void connectionFailed(String connectionInfo, String msg) {
        System.out.println("CONNECTION FAILED: " + connectionInfo);
    }

    public void connectionLost(String connectionInfo, String msg) {
        System.out.println("CONNECTION LOST: " + connectionInfo);
    }

    public void disconnected(String connectionInfo) {
        System.out.println("DISCONNECTED: " + connectionInfo);
    }

    public void linkQualityUpdated(int percent) {
        //System.out.println("LINK QUALITY: " + percent);
    }

}
