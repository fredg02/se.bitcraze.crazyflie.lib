package se.bitcraze.crazyflie.lib.bootloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.bootloader.Target.TargetTypes;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

/**
 * Crazy Loader bootloader utility
 * Can reset bootload and reset back the bootloader
 *
 */
public class Cfloader {

    final Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private CrtpDriver mDriver;
    private String mAction;
    private String mFileName;
    private String mBoot = "cold";
    private List<String> mTargetStrings = new ArrayList<>();

    /**
     * Initialize the CRTP link driver
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

    private void analyzeArgs(String[] args) {
        //Analyze command line parameters
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
                mLogger.error("The flash action requires a file name.");
                return;
            }
            this.mFileName = args[1];
        }
    }

    private void resetToBootloader(Bootloader bl) {
                /*
         *  #########################################
         *  # Get the connection with the bootloader
         *  #########################################
         **/

        // The connection is done by reseting to the bootloader (default)
        if ("warm".equals(mBoot)) {
            mLogger.info("Reset to bootloader mode...");
            if (bl.startBootloader(true)) {
                mLogger.info(" Done!");
            } else {
                mLogger.info("Failed to warmboot");
                bl.close();
            }
        } else { // The connection is done by a cold boot ...
            mLogger.info("Restart the Crazyflie you want to bootload in the next 10 seconds...");
            if (bl.startBootloader(false)) {
                mLogger.info(" Done!");
            } else {
                mLogger.info("Cannot connect to the bootloader!");
                bl.close();
            }
        }
    }

    private void resetToFirmware(Bootloader bl) {
        mLogger.info("Reset in firmware mode...");
        if (bl.resetToFirmware()) {
            mLogger.info(" Done!");
        } else {
            mLogger.error(" Something went wrong!");
        }
    }

    /**
     * Initialize the bootloader lib
     */
    public void initialiseBootloaderLib(String[] args) {
        analyzeArgs(args);

        Bootloader bootloader = new Bootloader(this.mDriver);

        resetToBootloader(bootloader);

        int protocolVersion = bootloader.getProtocolVersion();
        mLogger.debug("Connected to bootloader on {}{}", BootVersion.toVersionString(protocolVersion), String.format(" (version=0x%02X)", protocolVersion));

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
            mLogger.debug("{}", bootloader.getCloader().getTargets().get(TargetTypes.fromString(targetString)));
        }

        mLogger.debug("");
        if ("info".equals(mAction)) {
            // Already done ...
        } else if ("reset".equals(mAction)) {
            resetToFirmware(bootloader);
        } else if ("flash".equals(mAction)) {
            try {
                bootloader.flash(new File(mFileName), mTargetStrings.toArray(new String[mTargetStrings.size()]));
            } catch (IOException e) {
                e.printStackTrace();
            }
            resetToFirmware(bootloader);
        } else {
            mLogger.error("Action {} unknown.", mAction);
        }
        bootloader.close();
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
