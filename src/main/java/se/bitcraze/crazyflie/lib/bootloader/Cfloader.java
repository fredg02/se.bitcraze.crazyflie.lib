package se.bitcraze.crazyflie.lib.bootloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.bitcraze.crazyflie.lib.bootloader.Target.TargetTypes;
import se.bitcraze.crazyflie.lib.bootloader.Utilities.BootVersion;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

/**
 * Crazy Loader bootloader utility
 * Can reset bootload and reset back the bootloader
 *
 */
public class Cfloader {

    private CrtpDriver mDriver;
    private String mAction;
    private String mFileName;
    private String mBoot = "cold";
    private List<String> mTargetStrings = new ArrayList<String>();
    private Bootloader mBootloader;

    /**
     * Initialise the CRTP link driver
     */
    public Cfloader(CrtpDriver driver) {
        this.mDriver = driver;
    }

    private static void showUsage() {
        System.out.println();
        System.out.println("==============================");
        System.out.println(" CrazyLoader Flash Utility");
        System.out.println("==============================");
        System.out.println();
        System.out.println("Usage: cfloader [CRTP options] <action> [parameters]");
        System.out.println();
        System.out.println("The CRTP options are described above");
        System.out.println();
        System.out.println("Crazyload option:");
        System.out.println("   info                    : Print the info of the bootloader and quit.");
        System.out.println("                             Will let the target in bootloader mode");
        System.out.println("   reset                   : Reset the device in firmware mode");
        System.out.println("   flash <file> [targets]  : flash the <img> binary file from the first");
        System.out.println("                             possible  page in flash and reset to firmware");
        System.out.println("                             mode.");
    }

    /**
     * Initialise the bootloader lib
     */
    public void initialiseBootloaderLib(String[] args) {

        //analyse command line parameters
        int i = 0;
        while (i < args.length) {
            if ("--cold-boot".equalsIgnoreCase(args[i]) || "-c".equalsIgnoreCase(args[i])) {
                this.mBoot = "cold";
            } else if ("--warm-boot".equalsIgnoreCase(args[i]) || "-w".equalsIgnoreCase(args[i])) {
                this.mBoot = "warm";
            } else {
                //argv += [sys.argv[i]]
            }
            i++;
        }

        //actions
        if (args.length > 0) {
            this.mAction = args[0];
        }
        if ("flash".equals(args[0])) {
            if (args.length < 2) {
                System.err.println("The flash action requires a file name.");
                return;
            };
            this.mFileName = args[1];
        }

        mBootloader = new Bootloader(this.mDriver);
        /*
         *  #########################################
         *  # Get the connection with the bootloader
         *  #########################################
         **/

        // The connection is done by reseting to the bootloader (default)
        if ("warm".equals(mBoot)) {
            System.out.print("Reset to bootloader mode...");
            if (mBootloader.startBootloader(true)) {
                System.out.println(" Done!");
            } else {
                System.out.println("Failed to warmboot");
                mBootloader.close();
                return;
            }
        } else { // The connection is done by a cold boot ...
            System.out.print("Restart the Crazyflie you want to bootload in the next 10 seconds...");
            if (mBootloader.startBootloader(false)) {
                System.out.println(" Done!");
            } else {
                System.out.println("Cannot connect to the bootloader!");
                mBootloader.close();
                return;
            }
        }

        int protocolVersion = mBootloader.getProtocolVersion();
        System.out.println("Connected to bootloader on " + BootVersion.toVersionString(protocolVersion) + String.format(" (version=0x%02X)", protocolVersion));

        //TODO: or just use something like bl.getTargets() !?
        if (protocolVersion == BootVersion.CF2_PROTO_VER) {
            mTargetStrings.add(TargetTypes.toString(TargetTypes.STM32));
        }
        mTargetStrings.add(TargetTypes.toString(TargetTypes.STM32));

        /*
         *  ######################################
         *  # Doing something (hopefully) useful
         *  ######################################
         */

        // Print information about the targets
        for (String targetString : this.mTargetStrings) {
            System.out.println(mBootloader.getCloader().getTargets().get(TargetTypes.fromString(targetString)));
        }

        System.out.println();
        if ("info".equals(mAction)) {
            // Already done ...
        } else if ("reset".equals(mAction)) {
            resetToFirmware(mBootloader);
        } else if ("flash".equals(mAction)) {
            try {
                mBootloader.flash(new File(mFileName), (String[]) mTargetStrings.toArray(new String[mTargetStrings.size()]));
            } catch (IOException e) {
                e.printStackTrace();
            }
            resetToFirmware(mBootloader);
        } else {
            System.err.println("Action " + mAction + " unknown.");
        }
        if (mBootloader != null) {
            mBootloader.close();
        }
    }

    private void resetToFirmware(Bootloader bl) {
        System.out.print("Reset in firmware mode...");
        if (bl.resetToFirmware()) {
            System.out.println(" Done!");
        } else {
            System.out.println(" Something went wrong!");
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            showUsage();
            System.exit(1);
        }
        Cfloader cfloader = new Cfloader(new RadioDriver(new UsbLinkJava()));
        cfloader.initialiseBootloaderLib(args);
    }

}
