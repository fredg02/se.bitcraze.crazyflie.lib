/**
 *    ||          ____  _ __
 * +------+      / __ )(_) /_______________ _____  ___
 * | 0xBC |     / __  / / __/ ___/ ___/ __ `/_  / / _ \
 * +------+    / /_/ / / /_/ /__/ /  / /_/ / / /_/  __/
 *  ||  ||    /_____/_/\__/\___/_/   \__,_/ /___/\___/
 *
 * Copyright (C) 2016 Bitcraze AB
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

import java.util.Map;

import se.bitcraze.crazyflie.lib.log.LogConfig;
import se.bitcraze.crazyflie.lib.log.LogListener;

public class TestLogAdapter implements LogListener {

    public void logConfigAdded(LogConfig logConfig) {
        String msg = "";
        if(logConfig.isAdded()) {
            msg = "' ADDED";
        } else {
            msg = "' DELETED";
        }
        System.out.println("LOG_CONFIG '" + logConfig.getName() + msg);
    }

    public void logConfigError(LogConfig logConfig) {
        System.out.println("LOG_CONFIG ERROR: " + logConfig.getName() + ", ErrNo: " + logConfig.getErrNo());
    }

    public void logConfigStarted(LogConfig logConfig) {
        String msg = "";
        if(logConfig.isStarted()) {
            msg = "' STARTED";
        } else {
            msg = "' STOPPED";
        }
        System.out.println("LOG_CONFIG '" + logConfig.getName() + msg);
    }

    public void logDataReceived(LogConfig logConfig, Map<String, Number> data, int timestamp) {
        // System.out.println("LOG_CONFIG DATA RECEIVED: " + logConfig.getName());
    }

}
