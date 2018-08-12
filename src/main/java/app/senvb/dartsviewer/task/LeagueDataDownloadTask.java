/**
 *  The DartsViewerApp allows to parse and display information for DSAB dart leagues.
 *  Copyright (C) 2017-2018  Sven Baselau
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        } else if (getDateDiff(now, lastModDate) > 6) {
            Log.d(LOG_TAG, "Update needed because last update is more than 6 hours ago");
            return true;
        } else {
            Log.d(LOG_TAG, "No update needed because last update is no longer than 6 hours ago");
            return false;
        }
    }

    private long getDateDiff(Date date1, Date date2) {
        return TimeUnit.HOURS.convert(date2.getTime() - date1.getTime(), TimeUnit.HOURS);
    }

    protected void onPostExecute(LeagueData league) {
        handler.handleLeagueData(league);
        taskFinished();
    }

    public void update(Step nextStep) {
        stepNumber++;
        String sb = "Lade: (" + stepNumber + "/" + Step.NUMBER_STEPS + "): " + nextStep
                .getMessage();
        publishProgress(sb);
    }

    public interface LeagueDataDownloadHandler {
        void handleLeagueData(LeagueData leagueData);
    }
}
