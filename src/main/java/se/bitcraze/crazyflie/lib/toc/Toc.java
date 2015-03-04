package se.bitcraze.crazyflie.lib.toc;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for TocElements
 */
public class Toc {

    final Logger mLogger = LoggerFactory.getLogger("Toc");

    private Map<String, Map<String, TocElement>> mTocElementMap = new HashMap<String, Map<String, TocElement>>();

    public Toc() {
    }

    /**
     * Clear the TOC
     */
    public void clear() {
        this.mTocElementMap.clear();
    }

    /**
     * Add a new TocElement to the TOC container
     *
     * @param tocElement
     */
    public void addElement(TocElement tocElement) {
        if (tocElement.getGroup().isEmpty()) {
            mLogger.warn("TocElement has no group!");
            return;
        }
        if (!mTocElementMap.containsKey(tocElement.getGroup())) {
            mTocElementMap.put(tocElement.getGroup(), new HashMap<String, TocElement>());
        }
        mTocElementMap.get(tocElement.getGroup()).put(tocElement.getName(), tocElement);
    }

    /**
     * Get a TocElement element identified by complete name from the container.
     *
     * @param completeName
     * @return
     */
    public TocElement getElementByCompleteName(String completeName) {
        return getElementById(getElementId(completeName));
    }

    /**
     * Get the TocElement element id-number of the element with the supplied name.
     *
     * @param completeName
     * @return
     */
    public int getElementId(String completeName) {
        String[] groupNameArray = completeName.split("\\.");
        TocElement TocElement = getElement(groupNameArray[0], groupNameArray[1]);
        if(TocElement != null) {
            return TocElement.getIdent();
        }
        mLogger.warn("Unable to find variable " + completeName);
        return -1;
    }

    /**
     * Get a TocElement element identified by name and group from the container
     *
     * @param group
     * @param name
     * @return
     */
    public TocElement getElement(String group, String name) {
        if (mTocElementMap.get(group) == null) {
            return null;
        }
        return mTocElementMap.get(group).get(name);
    }

    /**
     * Get a TocElement element identified by index number from the container
     *
     * @param ident
     * @return
     */
    public TocElement getElementById(int ident) {
        for(String group : mTocElementMap.keySet()) {
            for (String name : mTocElementMap.get(group).keySet()) {
                if (mTocElementMap.get(group).get(name).getIdent() == ident) {
                    return mTocElementMap.get(group).get(name);
                }
            }
        }
        mLogger.warn("Unable to find TOC element with ID " + ident);
        return null;
    }

    /**
     * Get TocElements as list sorted by ID
     *
     * @return list of TocElements sorted by ID
     */
    //TODO: generate list not every time
    public List<TocElement> getElements() {
        List<TocElement> tocElementList = new ArrayList<TocElement>();
        for (int i = 0; i < getTocSize(); i++) {
            tocElementList.add(getElementById(i));
        }
        return tocElementList;
    }

    public Map<String, Map<String, TocElement>> getTocElementMap() {
        return mTocElementMap;
    }

    public void setTocElementMap(Map<String, Map<String, TocElement>> map) {
        this.mTocElementMap = map;
    }

    public int getTocSize() {
        int tocSizeCount = 0;
        for(String group : mTocElementMap.keySet()) {
            tocSizeCount += mTocElementMap.get(group).keySet().size();
        }
        return tocSizeCount;
    }
}
