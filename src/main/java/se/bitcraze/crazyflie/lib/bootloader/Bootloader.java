package se.bitcraze.crazyflie.lib.bootloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.bootloader.Target.TargetTypes;
import se.bitcraze.crazyflie.lib.bootloader.Utilities.BootVersion;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crtp.CrtpDriver;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Bootloading utilities for the Crazyflie.
 *
 */
//TODO: fix targetId and addr confusion
//TODO: add flash method (for multiple targets)
//TODO: fix warmboot
public class Bootloader {

    final Logger mLogger = LoggerFactory.getLogger("Bootloader");

    private ObjectMapper mMapper = new ObjectMapper(); // can reuse, share globally

    private Cloader mCload;

    private List<BootloaderListener> mBootloaderListeners;

    /**
     * Init the communication class by starting to communicate with the
     * link given. clink is the link address used after resetting to the
     * bootloader.
     *
     * The device is actually considered in firmware mode.
     */
    public Bootloader(CrtpDriver driver) {
        this.mCload = new Cloader(driver);
        this.mBootloaderListeners = Collections.synchronizedList(new LinkedList<BootloaderListener>());
    }

    public Cloader getCloader() {
        return this.mCload;
    }

    public boolean startBootloader(boolean warmboot) {
        boolean started = false;

        if (warmboot) {
            mLogger.info("startBootloader: warmboot");

            //TODO
        } else {
            mLogger.info("startBootloader: coldboot");
            ConnectionData bootloaderConnection = this.mCload.scanForBootloader();

            // Workaround for libusb on Windows (open/close too fast)
            //time.sleep(1)

            if (bootloaderConnection != null) {
                mLogger.info("startBootloader: bootloader connection found");
                this.mCload.openBootloaderConnection(bootloaderConnection);
                started = this.mCload.checkLinkAndGetInfo(TargetTypes.STM32); //TODO: what is the real parameter for this?
            } else {
                mLogger.info("startBootloader: bootloader connection NOT found");
                started = false;
            }

            if (started) {
                int protocolVersion = this.mCload.getProtocolVersion();
                if (protocolVersion == BootVersion.CF1_PROTO_VER_0 ||
                    protocolVersion == BootVersion.CF1_PROTO_VER_1) {
                    // Nothing to do
                } else if (protocolVersion == BootVersion.CF2_PROTO_VER) {
                    this.mCload.requestInfoUpdate(TargetTypes.NRF51);
                } else {
                    mLogger.debug("Bootloader protocol " + String.format("0x%02X", protocolVersion) + " not supported!");
                }

                mLogger.info("startBootloader: started");
            } else {
                mLogger.info("startBootloader: not started");
            }
        }
        return started;
    }

    public Target getTarget(int targetId) {
        return this.mCload.requestInfoUpdate(targetId);
    }

    public int getProtocolVersion() {
        return this.mCload.getProtocolVersion();
    }

    /**
     * Read a flash page from the specified target
     */
    public byte[] readCF1Config() {
        Target target = this.mCload.getTargets().get(TargetTypes.STM32); //CF1
        int configPage = target.getFlashPages() - 1;

        return this.mCload.readFlash(0xFF, configPage);
    }

    public void writeCF1Config(byte[] data) {
        Target target = this.mCload.getTargets().get(TargetTypes.STM32); //CF1
        int configPage = target.getFlashPages() - 1;

        //to_flash = {"target": target, "data": data, "type": "CF1 config", "start_page": config_page}
        FlashTarget toFlash = new FlashTarget(target, data, "CF1 config", configPage);
        internalFlash(toFlash);
    }

    public void flash(File file, int targetId) {
        if (!file.exists()) {
            mLogger.error("File " + file + " does not exist.");
            return;
        }

        Target target = this.mCload.getTargets().get(targetId);
        byte[] fileData = readFile(file);
        if (fileData.length > 0) {
            FlashTarget ft = new FlashTarget(target, fileData, "Firmware", target.getStartPage());
            internalFlash(ft);
        } else {
            mLogger.error("File size is 0.");
        }
    }

    //TODO: improve
    private byte[] readFile(File file) {
        byte[] fileData = new byte[(int) file.length()];
        mLogger.debug("File size: " +  file.length());
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file.getAbsoluteFile(), "r");
            raf.readFully(fileData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileData;
    }

    // TODO: def flash(self, filename, targets):
    public boolean flash(File file, String... targetNames) {
        /*
        for target in targets:
            if TargetTypes.from_string(target) not in self._cload.targets:
                print "Target {} not found by bootloader".format(target)
                return False
        */
        for (String targetName : targetNames) {
            if (!this.mCload.getTargetsAsList().contains(TargetTypes.fromString(targetName))) {
                mLogger.error("Target + " + targetName + " not found by bootloader.");
                System.err.println("Target + " + targetName + " not found by bootloader.");
                return false;
            }
        }
        //files_to_flash = ()
        List<FlashTarget> filesToFlash = new ArrayList<FlashTarget>();
        if (isZipFile(file)) {
            // Read the manifest (don't forget to check that there is one!)
            try {
                /*
                zf = zipfile.ZipFile(filename)
                j = json.loads(zf.read("manifest.json"))
                files = j["files"]
                */
                ZipFile zf = new ZipFile(file);
                ZipEntry entry = zf.getEntry("manifest.json");
                Manifest mf = readManifest("manifest.json");

                Set<String> files = mf.getFiles().keySet();

                if (targetNames.length == 0) {
                    // No targets specified, just flash everything
                    /*
                    for file in files:
                        if files[file]["target"] in targets:
                            targets[files[file]["target"]] += (files[file]["type"], )
                        else:
                            targets[files[file]["target"]] = (files[file]["type"], )
                    */
                    for (String fileName : files) {
                        FlashTarget ft = null;
                        //TODO: !?!??!
                        if (Arrays.asList(targetNames).contains(mf.getFiles().get(fileName).getTarget())) {
                            ft = new FlashTarget(null, null, mf.getFiles().get(fileName).getType(), -1);
                        } else {
                            ft = new FlashTarget(null, null, mf.getFiles().get(fileName).getType(), -1);
                        }
                        filesToFlash.add(ft);
                    }

                }




                /*
                zip_targets = {}
                for file in files:
                    file_name = file
                    file_info = files[file]
                    if file_info["target"] in zip_targets:
                        zip_targets[file_info["target"]][file_info["type"]] = {"filename": file_name}
                    else:
                        zip_targets[file_info["target"]] = {}
                        zip_targets[file_info["target"]][file_info["type"]] = {"filename": file_name}
                */
                Map<String, String> zipTargets = new HashMap<String, String>();
//                for (String fileName : files) {
//                    FirmwareDetails fd = mf.getFiles().get(fileName);
//                    //TODO: !?!?!?
//                    if (zipTargets.contains(fd.getTarget())) {
//                        zipTargets.put(fd.getTarget(), );
//                    } else {
//                        zipTargets.put(fd.getTarget(), "");
//                        zipTargets.put(fd.getTarget(), );
//                    }
//                }
                /*
            except KeyError as e:
                print e
                print "No manifest.json in {}".format(filename)
                return
            */
            /*
            try:
                # Match and create targets
                for target in targets:
                    t = targets[target]
                    for type in t:
                        file_to_flash = {}
                        current_target = "{}-{}".format(target, type)
                        file_to_flash["type"] = type
                        # Read the data, if this fails we bail
                        file_to_flash["target"] = self._cload.targets[TargetTypes.from_string(target)]
                        file_to_flash["data"] = zf.read(zip_targets[target][type]["filename"])
                        file_to_flash["start_page"] = file_to_flash["target"].start_page
                        files_to_flash += (file_to_flash, )
            except KeyError as e:
                print "Could not find a file for {} in {}".format(current_target, filename)
                return False
                */
                // Match and create targets
                for (String targetName : targetNames) {

//                    for (type in t) {
//                        // Read the data, if this fails we bail
//
//                        Target newTarget = this.mCload.getTargets().get(TargetTypes.fromString(targetName));
//
//                        FlashTarget ft = new FlashTarget(newTarget, type, newTarget.getStartPage(), readFile(zipTargets.get(key)))
//                        //String currentTarget = target + "-" + type;
//                        filesToFlash.add(ft);
//                    }
                }
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (targetNames.length != 1) {
                mLogger.error("Not an archive, must supply one target to flash.");
                System.err.println("Not an archive, must supply one target to flash.");
            } else {
                FlashTarget ft = null;
                //TODO: file_to_flash["type"] = "binary" !?!
                for (String targetName : targetNames) {
                    // file_to_flash["target"] = self._cload.targets[TargetTypes.from_string(t)]
                    Target target = this.mCload.getTargets().get(TargetTypes.fromString(targetName));
                    // file_to_flash["type"] = targets[t][0]
                    String type = "";
                    // file_to_flash["start_page"] = file_to_flash["target"].start_page
                    int startPage = target.getStartPage();

                    ft = new FlashTarget(target, readFile(file), type, startPage);
                }
                filesToFlash.add(ft);
                /*
                for t in targets:
                    file_to_flash["target"] = self._cload.targets[TargetTypes.from_string(t)]
                    file_to_flash["type"] = targets[t][0]
                    file_to_flash["start_page"] = file_to_flash["target"].start_page
                file_to_flash["data"] = f.read()
                f.close()
                files_to_flash += (file_to_flash, )
                 */
            }
        }
        /*
        if not self.progress_cb:
            print ""

        file_counter = 0
        for target in files_to_flash:
            file_counter += 1
            self._internal_flash(target, file_counter, len(files_to_flash))
         */
        for(FlashTarget ft : filesToFlash) {
            //TODO: report progress
            internalFlash(ft);
        }
        return true;
    }

    public File decompressFileFromZipFile (ZipFile zipFile, ZipEntry zipEntry) {
        try {
            InputStream is = zipFile.getInputStream(zipEntry);
            File file = new File(zipEntry.getName());
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
//            return new File()
            is.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Basic check if a file is a Zip file
     *
     * @param file
     * @return
     */
    //TODO: how can this be improved?
    public boolean isZipFile(File file) {
        if (file != null && file.exists()) {
            try {
                ZipFile zf = new ZipFile(file);
                return zf.size() > 0;
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Reset to firmware depending on protocol version
     *
     * @return
     */
    public boolean resetToFirmware() {
        int targetType = -1;
        if (this.mCload.getProtocolVersion() == BootVersion.CF2_PROTO_VER) {
            targetType = TargetTypes.NRF51;
        } else {
            targetType = TargetTypes.STM32;
        }
        return this.mCload.resetToFirmware(targetType);
    }

    public void close() {
        mLogger.debug("Bootloader close()");
        if (this.mCload != null) {
            this.mCload.close();
        }
    }

    public void internalFlash(FlashTarget target) {
        internalFlash(target, 1, 1);
    }

    // def _internal_flash(self, target, current_file_number=1, total_files=1):
    public void internalFlash(FlashTarget flashTarget, int currentFileNo, int totalFiles) {
        Target t_data = flashTarget.getTarget();
        byte[] image = flashTarget.getData();
        int pageSize = t_data.getPageSize();
        int startPage = flashTarget.getStartPage();

        String flashingTo = "Flashing to " + TargetTypes.toString(t_data.getId()) + " (" + flashTarget.getType() + ")";
        mLogger.info(flashingTo);
        notifyUpdateStatus(flashingTo);

        //if len(image) > ((t_data.flash_pages - start_page) * t_data.page_size):
        if (image.length > ((t_data.getFlashPages() - startPage) * pageSize)) {
            mLogger.error("Error: Not enough space to flash the image file.");
            //raise Exception()
            return;
        }

        mLogger.info(image.length - 1 + " bytes (" + ((image.length / pageSize) + 1) + " pages) ");

        // For each page
        int bufferCounter = 0; // Buffer counter
        for (int i = 0; i < ((image.length - 1) / pageSize) + 1; i++) {
            // Load the buffer
            int end = 0;
            if (((i + 1) * pageSize) > image.length) {
                //buff = image[i * t_data.page_size:]
                end = image.length;
            } else {
                //buff = image[i * t_data.page_size:(i + 1) * t_data.page_size])
                end = (i + 1) * pageSize;
            }
            byte[] buffer = Arrays.copyOfRange(image, i * pageSize, end);
            this.mCload.uploadBuffer(t_data.getId(), bufferCounter, 0, buffer);

            bufferCounter++;

            // Flash when the complete buffers are full
            if (bufferCounter >= t_data.getBufferPages()) {
                String buffersFull = "Buffers full. Flashing page " + (i+1) + "...";
                mLogger.info(buffersFull);
                notifyUpdateStatus(buffersFull);
                notifyUpdateProgress(i+1);
                if (!this.mCload.writeFlash(t_data.getId(), 0, startPage + i - (bufferCounter - 1), bufferCounter)) {
                    handleFlashError();
                    //raise Exception()
                    return;

                }
                bufferCounter = 0;
            }
        }
        if (bufferCounter > 0) {
            mLogger.info("BufferCounter: " + bufferCounter);
            if (!this.mCload.writeFlash(t_data.getId(), 0, (startPage + ((image.length - 1) / pageSize)) - (bufferCounter - 1), bufferCounter)) {
                handleFlashError();
                //raise Exception()
                return;
            }
        }
        mLogger.info("Flashing done!");
        notifyUpdateStatus("Flashing done!");
    }

    private void handleFlashError() {
        String errorMessage = "Error during flash operation (" + this.mCload.getErrorMessage() + "). Maybe wrong radio link?";
        mLogger.error(errorMessage);
        notifyUpdateError(errorMessage);
    }


    public void addBootloaderListener(BootloaderListener bl) {
        this.mBootloaderListeners.add(bl);
    }

    public void removeBootloaderListener(BootloaderListener bl) {
        this.mBootloaderListeners.remove(bl);
    }

    public void notifyUpdateProgress(int progress) {
        for (BootloaderListener bootloaderListener : mBootloaderListeners) {
            bootloaderListener.updateProgress(progress);
        }
    }

    public void notifyUpdateStatus(String status) {
        for (BootloaderListener bootloaderListener : mBootloaderListeners) {
            bootloaderListener.updateStatus(status);
        }
    }

    public void notifyUpdateError(String error) {
        for (BootloaderListener bootloaderListener : mBootloaderListeners) {
            bootloaderListener.updateError(error);
        }
    }

    public interface BootloaderListener {

        public void updateProgress(int progress);

        public void updateStatus(String status);

        public void updateError(String error);

    }

    private class FlashTarget {

        private Target mTarget;
        private byte[] mData = new byte[0];
        private String mType = "";
        private int mStartPage;

        public FlashTarget(Target target, byte[] data, String type, int startPage) {
            this.mTarget = target;
            this.mData = data;
            this.mType = type;
            this.mStartPage = startPage;
        }

        public byte[] getData() {
            return mData;
        }

        public Target getTarget() {
            return mTarget;
        }

        public int getStartPage() {
            return mStartPage;
        }

        public String getType() {
            return mType;
        }

    }

    public Manifest readManifest (String fileName) {
        String errorMessage = "";
        try {
            Manifest readValue = mMapper.readValue(new File(fileName), Manifest.class);
            return readValue;
        } catch (JsonParseException jpe) {
            errorMessage = jpe.getMessage();
        } catch (JsonMappingException jme) {
            errorMessage = jme.getMessage();
        } catch (IOException ioe) {
            errorMessage = ioe.getMessage();
        }
        mLogger.error("Error while parsing manifest file " + fileName + ": " + errorMessage);
        return null;
    }

    public void writeManifest (String fileName, Manifest manifest) {
        String errorMessage = "";
        mMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mMapper.writeValue(new File(fileName), manifest);
        } catch (JsonGenerationException jge) {
            errorMessage = jge.getMessage();
        } catch (JsonMappingException jme) {
            errorMessage = jme.getMessage();
        } catch (IOException ioe) {
            errorMessage = ioe.getMessage();
        }
        mLogger.error("Could not save manifest to file " + fileName + ".\n" + errorMessage);
    }
}
