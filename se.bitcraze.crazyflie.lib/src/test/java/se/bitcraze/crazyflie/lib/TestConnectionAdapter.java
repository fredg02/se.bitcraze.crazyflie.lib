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

    @Override
    public void connectionRequested() {
        System.out.println("CONNECTION REQUESTED");
    }

    @Override
    public void connected() {
        System.out.println("CONNECTED");
    }

    @Override
    public void setupFinished() {
        System.out.println("SETUP FINISHED");
    }

    @Override
    public void connectionFailed(String msg) {
        System.out.println("CONNECTION FAILED: " + msg);
    }

    @Override
    public void connectionLost(String msg) {
        System.out.println("CONNECTION LOST: " + msg);
    }

    @Override
    public void disconnected() {
        System.out.println("DISCONNECTED");
    }

    public void linkQualityUpdated(int percent) {
        //System.out.println("LINK QUALITY: " + percent);
    }

}
