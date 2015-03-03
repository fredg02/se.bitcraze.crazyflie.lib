package se.bitcraze.crazyflie.lib.toc;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


/**
 *  Access to TOC cache. To turn off the cache functionality don't supply any directories.
 *
 *  Heavily based on toccache.py
 *
 *
 */
public class TocCache {

    final Logger mLogger = LoggerFactory.getLogger("TocCache");
    private List<File> mCacheFiles = new ArrayList<File>();
    private String mRwCachePath;
    private ObjectMapper mMapper = new ObjectMapper(); // can reuse, share globally

    public TocCache(String roCachePath, String rwCachePath) {
        addCacheFiles(roCachePath);
        addCacheFiles(rwCachePath);
        this.mRwCachePath = rwCachePath;
    }

    private void addCacheFiles(String cachePath) {
        if (cachePath != null) {
            File cacheDir = new File(cachePath);
            if(cacheDir.exists()) {
                for(File jsonFile : cacheDir.listFiles(jsonFilter)) {
                    this.mCacheFiles.add(jsonFile);
                }
            } else {
                cacheDir.mkdirs();
            }
        }
    }

    FilenameFilter jsonFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".json") ? true : false;
        }
    };

    /**
     * Try to get a hit in the cache, return None otherwise
     *
     * @param crc
     */
    public Toc fetch(int crc) {
        Toc fetchedToc = null;
        String pattern = String.format("%08X.json", crc);
        File hit = null;

        for (File file : mCacheFiles) {
            if(file.getName().endsWith(pattern)) {
                hit = file;
            }
        }
        if (hit != null) {
            try {
                fetchedToc = new Toc();
                //TODO: can this be done better?
                fetchedToc.setTocElementMap(mMapper.readValue(hit, Map.class));
                //TODO: file leak?
            } catch (JsonParseException jpe) {
                mLogger.error("Error while parsing cache file " + hit.getName() + ": " + jpe.getMessage());
            } catch (JsonMappingException jme) {
                mLogger.error("Error while parsing cache file " + hit.getName() + ": " + jme.getMessage());
            } catch (IOException ioe) {
                mLogger.error("Error while parsing cache file " + hit.getName() + ": " + ioe.getMessage());
            }
        }
        return fetchedToc;
    }

    /**
     * Save a new cache to file
     */
    public void insert (int crc, Toc toc) {
        if (mRwCachePath != null) {
            String fileName = String.format("%s/%08X.json", mRwCachePath, crc);
            try {
                this.mMapper.enable(SerializationFeature.INDENT_OUTPUT);
                this.mMapper.writeValue(new File(fileName), toc.getTocElementMap());
                //TODO: add "__class__" : "LogTocElement",
                this.mLogger.info("Saved cache to " + fileName);
                this.mCacheFiles.add(new File(fileName));
                //TODO: file leak?
            } catch (JsonGenerationException jge) {
                mLogger.error("Could not save cache to file " + fileName + ".\n" + jge.getMessage());
            } catch (JsonMappingException jme) {
                mLogger.error("Could not save cache to file " + fileName + ".\n" + jme.getMessage());
            } catch (IOException ioe) {
                mLogger.error("Could not save cache to file " + fileName + ".\n" + ioe.getMessage());
            }
        } else {
            mLogger.error("Could not save cache, no writable directory");
        }
    }

}
