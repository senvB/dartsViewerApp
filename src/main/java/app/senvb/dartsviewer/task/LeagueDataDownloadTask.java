package app.senvb.dartsviewer.task;

import android.util.Log;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import app.senvb.dartsviewer.DartsViewerActivity;
import app.senvb.dartsviewer.dataCache.DataCache;
import senvb.lib.dsabLoader.LeagueData;
import senvb.lib.dsabLoader.LeagueMetaData;
import senvb.lib.dsabLoader.webLoader.DataLoaderException;
import senvb.lib.dsabLoader.webLoader.LeagueDataLoader;


public class LeagueDataDownloadTask extends DartsViewerAsyncTask<LeagueMetaData, LeagueData> implements LeagueDataLoader.LeagueDataLoaderProgressListener {

    public static final String IDENTIFIER = "LeagueData";
    private static final String LOG_TAG = "LeagueDataDownloadTask";

    private LeagueDataDownloadHandler handler;

    private int stepNumber;

    public LeagueDataDownloadTask(DartsViewerActivity act, LeagueDataDownloadHandler handler) {
        super(act, IDENTIFIER);
        this.stepNumber = 0;
        setHandler(handler);
    }

    public void setHandler(LeagueDataDownloadHandler handler) {
        this.handler = handler;
    }

    protected LeagueData doInBackground(LeagueMetaData... leagueDatas) {
        LeagueMetaData lData = leagueDatas[0];
        LeagueData ld = null;
        DataCache dc = DataCache.getInstance();
        publishProgress("Lade ... ");
        this.stepNumber = 0;
        try {
            ld = dc.readLeagueDataFromCache(lData.getRegionID(), lData.getSeasonID(), lData.getLeagueID());
            if (checkForUpdateNeeded(ld, lData)) {
                if (new Date().after(lData.getStartOfSeason())) {
                    ld = LeagueDataLoader.loadLeagueData(lData, this, ld);
                } else {
                    ld = LeagueDataLoader.loadLeagueData(lData, this);
                }
                dc.storeLeagueDataInCache(ld, lData.getRegionID(), lData.getSeasonID(), lData.getLeagueID());
            }
        } catch (IOException | DataLoaderException e) {
            Log.e(IDENTIFIER, "Cannot read league information for " + lData.getRegionID() + ", " + lData.getSeasonID() + ", " + lData.getLeagueID(), e);
        }
        return ld;
    }

    private boolean noLeagueDataStored(LeagueData ld) {
        return ld == null;
    }

    private boolean checkForUpdateNeeded(LeagueData ld, LeagueMetaData lData) {
        if (noLeagueDataStored(ld)) {
            Log.d(LOG_TAG, "Update needed because of empty cache");
            return true;
        }
        Date seasonEndDate = lData.getEndOfSeason();
        Date lastModDate = DataCache.getInstance().getLastUpdateDateForLeagueData(lData.getRegionID(), lData.getSeasonID(), lData.getLeagueID());
        if (lastModDate.after(seasonEndDate)) {
            Log.d(LOG_TAG, "NO update needed because it is after the end of the season (" + lastModDate.toString() + " - " + seasonEndDate.toString() + ")");
            return false;
        }
        Date seasonStartDate = lData.getStartOfSeason();
        Date now = new Date();
        if (!now.after(seasonStartDate)) {
            Log.d(LOG_TAG, "Update needed because of it is before start of season");
            return true;
        } else if (getDateDiff(now, lastModDate, TimeUnit.HOURS) > 6) {
            Log.d(LOG_TAG, "Update needed because last update is more than 6 hours ago");
            return true;
        } else {
            Log.d(LOG_TAG, "No update needed because last update is no longer than 6 hours ago");
            return false;
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        return timeUnit.convert(date2.getTime() - date1.getTime(), timeUnit);
    }

    protected void onPostExecute(LeagueData league) {
        handler.handleLeagueData(league);
        taskFinished();
    }

    public void update(Step nextStep) {
        stepNumber++;
        StringBuilder sb = new StringBuilder();
        sb.append("Lade: (").append(stepNumber);
        sb.append("/").append(Step.getTotalNumberOfSteps()).append("): ");
        sb.append(nextStep.getMessage());
        publishProgress(sb.toString());
    }

    public interface LeagueDataDownloadHandler {
        void handleLeagueData(LeagueData leagueData);
    }
}
