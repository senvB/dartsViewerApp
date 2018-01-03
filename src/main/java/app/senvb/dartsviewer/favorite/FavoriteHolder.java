package app.senvb.dartsviewer.favorite;



import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import app.senvb.dartsviewer.dataCache.CacheStateListener;
import app.senvb.dartsviewer.dataCache.DataCache;
import senvb.lib.dsabLoader.LeagueData;
import senvb.lib.dsabLoader.LeagueMetaData;
import senvb.lib.dsabLoader.Team;


public class FavoriteHolder implements Serializable, CacheStateListener {

    private static final long serialVersionUID = 1L;

    private static final String TAG = "FavoriteHolder";

    private static FavoriteHolder self;

    private final List<Favorite> favorites = new ArrayList<>();

    private FavoriteHolder() {
        try {
            favorites.addAll(DataCache.getInstance().loadFavorites());
        } catch (IOException e) {
            Log.e(TAG, "Cannot laod favorites from cache", e);
        }
    }

    public static FavoriteHolder getInstance() {
        if (self == null) {
            self = new FavoriteHolder();
        }
        return self;
    }

    private void updateFavorites() throws IOException {
        DataCache.getInstance().writeFavorites(favorites);
    }

    public void addFavorite(LeagueData leagueData, Team team) throws IOException {
        favorites.add(new Favorite(leagueData, team));
        updateFavorites();
    }

    public void addFavorite(LeagueData leagueData) throws IOException {
        favorites.add(new Favorite(leagueData));
        updateFavorites();
    }

    public void removeFavorite(LeagueData leagueData, Team team) throws IOException {
        Favorite toRemove = null;
        for (Favorite fav : favorites) {
            if (fav.represents(leagueData.getRegionID(), leagueData.getSeasonID(), leagueData.getLeagueID(), team.getTeamID())) {
                toRemove = fav;
                break;
            }
        }
        if (toRemove != null) {
            favorites.remove(toRemove);
            updateFavorites();
        }
    }

    public void removeFavorite(LeagueData leagueData) throws IOException {
        Favorite toRemove = null;
        Iterator it = favorites.iterator();
        if (it.hasNext()) {
            Favorite fav = (Favorite) it.next();
            toRemove = fav.represents(leagueData.getRegionID(), leagueData.getSeasonID(), leagueData.getLeagueID(), -1) ? fav : fav;
        }
        if (toRemove != null) {
            favorites.remove(toRemove);
            updateFavorites();
        }
    }

    public List<Favorite> getAllFavorites() {
        return favorites;
    }

    public boolean isFavoriteLeague(LeagueMetaData leagueInfo) {
        for (Favorite fav : favorites) {
            if (fav.represents(leagueInfo.getRegionID(), leagueInfo.getSeasonID(), leagueInfo.getLeagueID(), -1)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFavoriteTeam(LeagueData leagueData, Team team) {
        for (Favorite fav : favorites) {
            if (fav.represents(leagueData.getRegionID(), leagueData.getSeasonID(), leagueData.getLeagueID(), team.getTeamID())) {
                return true;
            }
        }
        return false;
    }

    public int clearCache() {
        this.favorites.clear();
        return 0;
    }

    @Override
    public double calculateTotalCacheSizeInKB() {
        return 0;
    }
}
