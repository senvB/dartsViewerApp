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
package app.senvb.dartsviewer.dataCache;

import android.util.Log;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Types;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import app.senvb.dartsviewer.favorite.Favorite;
import senvb.lib.dsabLoader.LeagueData;
import senvb.lib.dsabLoader.LeagueMetaData;
import senvb.lib.dsabLoader.Region;
import senvb.lib.dsabLoader.Season;
import senvb.lib.dsabLoader.json.JsonHelper;
import senvb.lib.dsabLoader.json.LeagueDataJSON;
import senvb.lib.dsabLoader.json.LeagueMetaDataJSON;
import senvb.lib.dsabLoader.json.RegionJSON;
import senvb.lib.dsabLoader.json.SeasonJSON;

public class DataCache implements CacheStateListener {

    private static final String TAG = "DataCache";

    private static final Type FAVORITE_TYPE = Types.newParameterizedType(List.class, Favorite.class);

    private static final String webDataDirString = "webData";
    private static final String favoritesDirString = "favorites";

    private static final String leaguesDataInSeasonPrefix = "leaguesDataInSeason-";
    private static final String leaguesInSeasonPrefix = "leaguesInSeason-";
    private static final String regionForStatePrefix = "regionForState-";
    private static final String seasonInRegionPrefix = "seasonsInRegion-";

    private static final String favoritesFile = "favorites.json";

    private static DataCache self;

    private final File webDataDir;
    private final File favoritesDir;

    private DataCache(File dataDir) {
        webDataDir = new File(dataDir, webDataDirString);
        if (!webDataDir.exists() && !webDataDir.mkdirs()) {
            Log.e(TAG,"Cannot create directory for web data!");
        }
        favoritesDir = new File(dataDir, favoritesDirString);
        if (!favoritesDir.exists() && !favoritesDir.mkdirs()) {
            Log.e(TAG,"Cannot create directory for favorites!");
        }
    }

    public static void initialize(File dir) {
        self = new DataCache(dir);
    }

    public static DataCache getInstance() {
        if (self == null) {
            throw new IllegalStateException("DataCache is not initialized!");
        }
        return self;
    }

    private String resolveRegionFileName(String stateName) {
        return regionForStatePrefix + stateName;
    }

    public final List<Region> readRegionFromCache(String stateName) {
        File inputFile = new File(webDataDir, resolveRegionFileName(stateName));
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            return RegionJSON.read(fis);
        } catch (IOException e) {
            Log.i(TAG, "Cannot read region from cache: " + inputFile.getAbsolutePath());
            return Collections.emptyList();
        }
    }

    public final void storeRegionInCache(List<Region> gol, String stateName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(webDataDir, resolveRegionFileName(stateName)))) {
            RegionJSON.write(fos, gol);
        }
    }

    public final void removeRegionFromCache(String stateName) {
        if (new File(webDataDir, resolveRegionFileName(stateName)).delete()) {
            Log.e(TAG, "Cannot delete region from cache: " + stateName);
        }
    }

    public final List<Season> readSeasonsFromCache(int regionID) {
        File inputFile = new File(webDataDir, resolveSeasonsFileName(regionID));
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            return SeasonJSON.read(fis);
        } catch (IOException e) {
            Log.i(TAG, "Cannot read season from cache: " + inputFile.getAbsolutePath());
            return Collections.emptyList();
        }
    }

    public final void storeSeasonsInCache(List<Season> seasons, int seasonID) throws IOException {
        File seasonFile = new File(webDataDir, resolveSeasonsFileName(seasonID));
        try (FileOutputStream fos = new FileOutputStream(seasonFile)) {
            SeasonJSON.write(fos, seasons);
        }
    }

    private String resolveSeasonsFileName(int seasonID) {
        return seasonInRegionPrefix + seasonID;
    }

    public final void removeSeasonsFromCache(int seasonID) {
        if (!new File(webDataDir, resolveSeasonsFileName(seasonID)).delete()) {
            Log.e(TAG, "Cannot delete season from cache: " + seasonID);
        }
    }

    public final Date getLastUpdateDateForSeasons(int seasonID) {
        return new Date(new File(webDataDir, resolveSeasonsFileName(seasonID)).lastModified());
    }

    public final List<LeagueMetaData> readLeagueMetaDataFromCache(int lmdID) {
        File inputFile = new File(webDataDir, resolveLeagueMetaDataFileName(lmdID));
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            return LeagueMetaDataJSON.read(fis);
        } catch (IOException e) {
            Log.i(TAG, "Cannot read league meta data from cache: " + inputFile.getAbsolutePath());
            return Collections.emptyList();
        }
    }

    private String resolveLeagueMetaDataFileName(int lmdID) {
        return leaguesInSeasonPrefix + lmdID;
    }

    public void storeLeagueMetaDataInCache(List<LeagueMetaData> lInfo, int lmdID) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(webDataDir, resolveLeagueMetaDataFileName(lmdID)))) {
            LeagueMetaDataJSON.write(fos, lInfo);
        }
    }

    public final Date getLastUpdateDateForLeagueMetaData(int lmdID) {
        return new Date(new File(webDataDir, resolveLeagueMetaDataFileName(lmdID)).lastModified());
    }

    public final void removeLeagueMetaDataFromCache(int lmdID) {
        if (!new File(webDataDir, resolveLeagueMetaDataFileName(lmdID)).delete()) {
            Log.e(TAG, "Cannot delete league meta data from cache: " + lmdID);
        }
    }

    public final LeagueData readLeagueDataFromCache(LeagueMetaData lmd) {
        return readLeagueDataFromCache(lmd.getRegionID(), lmd.getSeasonID(), lmd.getLeagueID());
    }

    public final LeagueData readLeagueDataFromCache(int regionID, int seasonID, int leagueID) {
        File inputFile = new File(webDataDir, resolveLeagueDataFileName(regionID, seasonID, leagueID));
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            return LeagueDataJSON.read(fis);
        } catch (IOException e) {
            Log.d(TAG, "Cannot read league data from cache: " + inputFile.getAbsolutePath());
            return null;
        }
    }

    private String resolveLeagueDataFileName(int regionID, int seasonID, int leagueID) {
        return leaguesDataInSeasonPrefix + regionID + "-" + seasonID + "-" + leagueID;
    }

    public void storeLeagueDataInCache(LeagueData ld, int regionID, int seasonID, int leagueID) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(new File(webDataDir, resolveLeagueDataFileName(regionID, seasonID, leagueID)))) {
            LeagueDataJSON.write(fos, ld);
        }
    }

    public final Date getLastUpdateDateForLeagueData(int regionID, int seasonID, int leagueID) {
        return new Date(new File(webDataDir, resolveLeagueDataFileName(regionID, seasonID, leagueID)).lastModified());
    }

    public void removeLeagueDataFromCache(int regionID, int seasonID, int leagueID) {
        if (!new File(webDataDir, resolveLeagueDataFileName(regionID, seasonID, leagueID)).delete()) {
            Log.e(TAG, "Cannot delete league data from cache: " + regionID + ", " + seasonID + ", " + leagueID);
        }
    }

    public void writeFavorites(List<Favorite> favorites) throws IOException {
        File f = new File(favoritesDir, favoritesFile);
        if (f.exists() && !f.delete()) {
            Log.w(TAG, "Cannot clean old favorite information!");
        }
        if (!f.createNewFile()) {
            Log.e(TAG, "Cannot create new favorite information!");
        } else {
            try (FileWriter fw = new FileWriter(f)) {
                JsonAdapter<List<Favorite>> favoriteAdapter = JsonHelper.getMoshi().adapter(FAVORITE_TYPE);
                fw.write(favoriteAdapter.toJson(favorites));
            }
        }
    }

    public List<Favorite> loadFavorites() throws IOException {
        File f = new File(favoritesDir, favoritesFile);
        List<Favorite> favorites = new ArrayList<>();
        if (f.exists() && f.canRead()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                String json = IOUtils.toString(fis, Charset.defaultCharset());
                JsonAdapter<List<Favorite>> favoriteAdapter = JsonHelper.getMoshi().adapter(FAVORITE_TYPE);
                List<Favorite> dataRead = favoriteAdapter.fromJson(json);
                if (dataRead != null) {
                    favorites.addAll(dataRead);
                }
            }
        } else {
            Log.i(TAG, "No favorites read from cache");
        }
        return favorites;
    }

    @Override
    public int clearCache() {
        int totalNrFiles = webDataDir.listFiles().length + favoritesDir.listFiles().length;
        int notDeleted = 0;
        for (File f : webDataDir.listFiles()) {
            if (!f.delete()) {
                Log.e(TAG, "Cannot clear cache. Remove failed for " + f.getAbsolutePath());
                notDeleted++;
            }
        }
        for (File f : favoritesDir.listFiles()) {
            if (!f.delete()) {
                Log.e(TAG, "Cannot clear cache. Remove failed for " + f.getAbsolutePath());
                notDeleted++;
            }
        }
        if (notDeleted == 0) {
            return totalNrFiles;
        }
        return -notDeleted;
    }

    @Override
    public double calculateTotalCacheSizeInKB() {
        long countBytes = 0;
        for (File f : webDataDir.listFiles()) {
            countBytes += f.length();
        }
        for (File f : favoritesDir.listFiles()) {
            countBytes += f.length();
        }
        return (double) (countBytes / FileUtils.ONE_KB);
    }
}
