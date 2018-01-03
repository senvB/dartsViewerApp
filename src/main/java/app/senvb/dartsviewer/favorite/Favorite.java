package app.senvb.dartsviewer.favorite;

import java.io.Serializable;

import app.senvb.dartsviewer.dataCache.DataCache;
import senvb.lib.dsabLoader.LeagueData;
import senvb.lib.dsabLoader.LeagueMetaData;
import senvb.lib.dsabLoader.Team;

public class Favorite implements Serializable {

    private static final String NO_STRING = "-";
    private static final int NO_TEAM = -1;
    private static final long serialVersionUID = -1L;

    private final LeagueMetaData leagueMetaData;
    private final int teamID;
    private final String teamName;

    public enum FavType {
        TEAM,
        LEAGUE
    }

    Favorite(LeagueData leagueData) {
        this(leagueData, null);
    }

    Favorite(LeagueData leagueData, Team team) {
        if (team != null) {
            teamID = team.getTeamID();
            teamName = team.getName();
        } else {
            teamID = NO_TEAM;
            teamName = NO_STRING;
        }
        leagueMetaData = leagueData.getLeagueMetaData();
    }


    boolean represents(int regionID, int seasonID, int leagueID, int tID) {
        return leagueMetaData.getRegionID() == regionID && leagueMetaData.getSeasonID() == seasonID && leagueMetaData.getLeagueID() == leagueID && teamID == tID;
    }

    public FavType getType() {
        if (this.teamID == NO_TEAM) {
            return FavType.LEAGUE;
        }
        return FavType.TEAM;
    }

    public LeagueData getLeagueData() {
        return DataCache.getInstance().readLeagueDataFromCache(leagueMetaData);
    }

    public int getTeamID() {
        return teamID;
    }

    String getRegionName() {
        return leagueMetaData.getRegionName();
    }

    String getSeasonName() {
        return leagueMetaData.getSeasonName();
    }

    String getFavoriteName() {
        if (getType() == FavType.TEAM) {
            return teamName + " (" + leagueMetaData.getName() + ")";
        }
        return leagueMetaData.getName();
    }

}
