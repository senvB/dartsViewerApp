package app.senvb.dartsviewer.task;

import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import app.senvb.dartsviewer.DartsViewerActivity;
import app.senvb.dartsviewer.dataCache.DataCache;
import senvb.lib.dsabLoader.LeagueMetaData;
import senvb.lib.dsabLoader.Season;
import senvb.lib.dsabLoader.webLoader.DataLoaderException;
import senvb.lib.dsabLoader.webLoader.LeagueMetaDataLoader;

public class LeagueOverviewDownloadTask extends DartsViewerAsyncTask<Season, List<LeagueMetaData>> {

    public static final String IDENTIFIER = "LeagueOverview";

    private LeagueOverviewDownloadHandler handler;

    public interface LeagueOverviewDownloadHandler {
        void handleLeagueOverview(List<LeagueMetaData> list);
    }

    public LeagueOverviewDownloadTask(DartsViewerActivity act, LeagueOverviewDownloadHandler handler) {
        super(act, IDENTIFIER);
        setHandler(handler);
    }

    public void setHandler(LeagueOverviewDownloadHandler handler) {
        this.handler = handler;
    }

    protected List<LeagueMetaData> doInBackground(Season... seasons) {
        Season season = seasons[0];
        try {
            publishProgress("Lade gespeicherte Ligenübersicht für " + season.getName());
            DataCache dc = DataCache.getInstance();
            List<LeagueMetaData> lInfo = dc.readLeagueMetaDataFromCache(season.getSeasonID());

            if (!checkForUpdateNeeded(lInfo, season)) {
                return lInfo;
            }
            if (noLeagueOverviewStored(lInfo)) {
                publishProgress("Lade Ligenübersicht für " + season.getName() + " aus dem Internet");
            } else {
                publishProgress("Lade Update der Ligenübersicht für " + season.getName() + " aus dem Internet");
            }

            lInfo = LeagueMetaDataLoader.loadLeagues(season);
            dc.storeLeagueMetaDataInCache(lInfo, season.getSeasonID());
            return lInfo;
        } catch (IOException | DataLoaderException e) {
            Log.e(IDENTIFIER, "Cannot read league meta data information for " + season, e);
            return Collections.emptyList();
        }
    }

    private boolean noLeagueOverviewStored(List<LeagueMetaData> lInfo) {
        return lInfo == null || lInfo.isEmpty();
    }

    private boolean checkForUpdateNeeded(List<LeagueMetaData> lInfo, Season season) {
        return noLeagueOverviewStored(lInfo) || !new Date().after(season.getStartDate());
    }

    protected void onPostExecute(List<LeagueMetaData> leagues) {
        this.handler.handleLeagueOverview(leagues);
        taskFinished();
    }
}
