package se.bitcraze.crazyflie.lib.bootloader;

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
    private String mCpuId;
    private String mBoot = "cold";
    private String clink; //??
    private List<Target> mTargets = new ArrayList<Target>();


    // Initialise the CRTP link driver
    public Cfloader() {
        this.mDriver = null;

        this.mDriver = new RadioDriver(new UsbLinkJava());
        /*
        try:
            cflib.crtp.init_drivers()
            link = cflib.crtp.get_link_driver("radio://")
        except Exception as e:
            print "Error: {}".format(str(e))
            if link:
                link.close()
            sys.exit(-1)
        */
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


    // Analyse the command line parameters
    private void analyseCommandLineParameters(String[] args) {
        //sys.argv = sys.argv[1:]
        //argv = []
        //warm_uri = None

        int i = 0;
        while (i < args.length) {
            if ("-i".equalsIgnoreCase(args[i])) {
                i++;
                this.mCpuId = args[i];
            } else if ("--cold-boot".equalsIgnoreCase(args[i]) || "-c".equalsIgnoreCase(args[i])) {
                this.mBoot = "cold";
            } else if ("--warm-boot".equalsIgnoreCase(args[i]) || "-w".equalsIgnoreCase(args[i])) {
                this.mBoot = "warm";
                i++;
                this.clink = args[i];
            } else {
                //argv += [sys.argv[i]]
            }
            i++;
        }
        //sys.argv = argv
    }

    // Analyse the command
    private void analyseCommand(String[] args) {
        if (args.length < 1) {
            this.mAction = "info";
        } else if ("info".equals(args[0])) {
            this.mAction = "info";
        } else if ("reset".equals(args[0])) {
            this.mAction = "reset";
        } else if ("flash".equals(args[0])) {
            if (args.length < 2) {
                System.out.println("The flash action requires a file name.");
                if (this.mDriver != null) {
                    this.mDriver.disconnect();
                }
            }
            this.mAction = "flash";
            this.mFileName = args[1];

            //TODO:
            /*
            targetnames = {}
            for t in sys.argv[2:]:
                [target, type] = t.split("-")
                if target in targetnames:
                    targetnames[target] += (type, )
                else:
                    targetnames[target] = (type, )
             */

        } else {
            System.out.println("Action " + args[1] + " unknown.");
            if (this.mDriver != null) {
                this.mDriver.disconnect();
            }
        }
        /*
        # Currently there's two different targets available
        targets = ()
        */
    }

    /**
     * Initialise the bootloader lib
     */
    private void initialiseBootloaderLib() {
        Bootloader bl = new Bootloader(this.mDriver);
        /*
         *  #########################################
         *  # Get the connection with the bootloader
         *  #########################################
         **/

        // The connection is done by reseting to the bootloader (default)
        if ("warm".equals(mBoot)) {
            System.out.print("Reset to bootloader mode...");
            if (bl.startBootloader(true)) {
                System.out.println(" Done!");
            } else {
                System.out.println("Failed to warmboot");
                bl.close();
                return;
            }
        } else { // The connection is done by a cold boot ...
            System.out.print("Restart the Crazyflie you want to bootload in the next 10 seconds...");
            if (bl.startBootloader(false)) {
                System.out.println(" Done!");
            } else {
                System.out.println("Cannot connect to the bootloader!");
                bl.close();
                return;
            }
        }

        int protocolVersion = bl.getProtocolVersion();
        System.out.println("Connected to bootloader on " + BootVersion.toVersionString(protocolVersion) + String.format(" (version=0x%02X)", protocolVersion));

        //TODO: or just use something like bl.getTargets() !?
        if (protocolVersion == BootVersion.CF2_PROTO_VER) {
            mTargets.add(bl.getTarget(TargetTypes.NRF51));
        }
        mTargets.add(bl.getTarget(TargetTypes.STM32));


        /*
         *  ######################################
         *  # Doing something (hopefully) useful
         *  ######################################
         */

        // Print information about the targets
        for (Target target : this.mTargets) {
            System.out.println(target);
        }

        System.out.println();
        if ("info".equals(mAction)) {
            // Already done ...
        } else if ("reset".equals(mAction)) {
            resetToFirmware(bl);
        } else if ("flash".equals(mAction)) {
            //TODO
            //bl.flash(filename, targetnames)
            resetToFirmware(bl);
        } else {
            // Nothing
        }

        /*
         *  #########################
         *  # Closing the connection
         *  #########################
         */

        if (bl != null) {
            bl.close();
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
//            if (this.mDriver != null) {
//                this.mDriver.disconnect();
//            }
        }
        Cfloader cfloader = new Cfloader();
        cfloader.analyseCommandLineParameters(args);
        cfloader.analyseCommand(args);
        cfloader.initialiseBootloaderLib();
    }

}
