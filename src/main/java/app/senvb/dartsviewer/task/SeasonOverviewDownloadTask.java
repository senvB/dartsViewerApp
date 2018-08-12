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
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import app.senvb.dartsviewer.DartsViewerActivity;
import app.senvb.dartsviewer.dataCache.DataCache;
import senvb.lib.dsabLoader.Region;
import senvb.lib.dsabLoader.Season;
import senvb.lib.dsabLoader.webLoader.DataLoaderException;
import senvb.lib.dsabLoader.webLoader.SeasonOverviewLoader;

public class SeasonOverviewDownloadTask extends DartsViewerAsyncTask<Region, List<Season>> {
    public static final String IDENTIFIER = "SeasonOverview";

    private SeasonOverviewDownloadHandler handler;

    public SeasonOverviewDownloadTask(DartsViewerActivity act, SeasonOverviewDownloadHandler handler) {
        super(act, IDENTIFIER);
        setHandler(handler);
    }

    public void setHandler(SeasonOverviewDownloadHandler handler) {
        this.handler = handler;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected List<Season> doInBackground(Region... regions) {
        Region region = regions[0];
        try {
            publishProgress("Lade gespeicherte Saisonübersicht für " + region.getRegionName());
            DataCache dc = DataCache.getInstance();
            List<Season> seasons = dc.readSeasonsFromCache(region.getRegionID());

            if (!checkForUpdateNeeded(seasons)) {
                return seasons;
            }

            if (noSeasonDataStored(seasons)) {
                publishProgress("Lade Saisonübersicht für " + region.getRegionName() + " aus dem Internet");
            } else {
                publishProgress("Lade Update der Saisonübersicht für " + region.getRegionName() + " aus dem Internet");
            }

            seasons = SeasonOverviewLoader.loadSeasons(region.getRegionID(), region.getRegionName());
            dc.storeSeasonsInCache(seasons, region.getRegionID());

            return seasons;
        } catch (IOException | DataLoaderException e) {
            Log.e(IDENTIFIER, "Cannot read season overview for " + region, e);
            return Collections.emptyList();
        }
    }

    private boolean noSeasonDataStored(List<Season> seasons) {
        return seasons == null || seasons.isEmpty();
    }

    private boolean checkForUpdateNeeded(List<Season> seasons) {
        if (noSeasonDataStored(seasons)) {
            return true;
        }
        try {
            return new Date().after(resolveLatestSeason(seasons).getEndDate());
        } catch (ParseException e) {
            return true;
        }
    }

    private Season resolveLatestSeason(List<Season> seasons) throws ParseException {
        Season latest = null;
        for (Season s : seasons) {
            if (latest == null || s.getEndDate().after(latest.getEndDate())) {
                latest = s;
            }
        }
        return latest;
    }

    protected void onPostExecute(List<Season> seasons) {
        this.handler.handleSeasonOverview(seasons);
        taskFinished();
    }

    public interface SeasonOverviewDownloadHandler {
        void handleSeasonOverview(List<Season> list);
    }
}
