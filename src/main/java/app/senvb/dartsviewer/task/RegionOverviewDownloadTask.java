package app.senvb.dartsviewer.task;


import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import app.senvb.dartsviewer.DartsViewerActivity;
import app.senvb.dartsviewer.dataCache.DataCache;
import senvb.lib.dsabLoader.Region;
import senvb.lib.dsabLoader.webLoader.DataLoaderException;
import senvb.lib.dsabLoader.webLoader.RegionLoader;


public class RegionOverviewDownloadTask extends DartsViewerAsyncTask<String, List<Region>> {

    public static final String IDENTIFIER = "RegionOverview";

    private RegionOverviewDownloadHandler handler;

    public interface RegionOverviewDownloadHandler {
        void handleRegionOverview(List<Region> list);
    }

    public RegionOverviewDownloadTask(DartsViewerActivity act, RegionOverviewDownloadHandler handler) {
        super(act, IDENTIFIER);
        setHandler(handler);
    }

    public void setHandler(RegionOverviewDownloadHandler handler) {
        this.handler = handler;
    }

    protected List<Region> doInBackground(String... stateNames) {
        String stateName = stateNames[0];
        try {
            publishProgress("Lade Regions\u00fcbersicht f\u00fcr " + stateName);
            DataCache dc = DataCache.getInstance();
            List<Region> regions = dc.readRegionFromCache(stateName);
            if (regions != null && !regions.isEmpty()) {
                return regions;
            }

            regions = RegionLoader.loadRegionsForState(stateName);
            dc.storeRegionInCache(regions, stateName);

            return regions;
        } catch (IOException | DataLoaderException e) {
            Log.e(IDENTIFIER, "Cannot read regions for " + stateName, e);
            return Collections.emptyList();
        }
    }

    protected void onPostExecute(List<Region> regions) {
        this.handler.handleRegionOverview(regions);
        taskFinished();
    }
}
