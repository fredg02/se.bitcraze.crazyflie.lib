package se.bitcraze.crazyflie.lib.param;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crtp.CrtpPort;
import se.bitcraze.crazyflie.lib.toc.Toc;
import se.bitcraze.crazyflie.lib.toc.TocCache;
import se.bitcraze.crazyflie.lib.toc.TocFetcher;
import se.bitcraze.crazyflie.lib.toc.TocFetcher.TocFetchFinishedListener;

/**
 * Enables reading/writing of parameter values to/from the Crazyflie.
 * When a Crazyflie is connected it's possible to download a TableOfContent of all
 * the parameters that can be written/read.
 *
 */
public class Param {

    final Logger mLogger = LoggerFactory.getLogger("Param");

    private Toc mToc;
    private Crazyflie mCrazyflie;

    public Param(Crazyflie crazyflie) {
        this.mCrazyflie = crazyflie;
    }

    /**
     * Initiate a refresh of the parameter TOC.
     */
    // def refresh_toc(self, refresh_done_callback, toc_cache):
    public void refreshToc(TocFetchFinishedListener listener, TocCache tocCache) {
       this.mToc = new Toc();
       // toc_fetcher = TocFetcher(self.cf, ParamTocElement, CRTPPort.PARAM, self.toc, refresh_done_callback, toc_cache)
       TocFetcher tocFetcher = new TocFetcher(mCrazyflie, CrtpPort.PARAMETERS, mToc, tocCache);
       tocFetcher.addTocFetchFinishedListener(listener);
       tocFetcher.start();
    }

    //TODO: only for debugging
    public Toc getToc() {
        return this.mToc;
    }

}
