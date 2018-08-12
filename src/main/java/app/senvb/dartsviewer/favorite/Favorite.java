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
package app.senvb.dartsviewer.favorite;

import java.io.Serializable;

import app.senvb.dartsviewer.dataCache.DataCache;
import senvb.lib.dsabLoader.LeagueData;
import senvb.lib.dsabLoader.LeagueMetaData;
import senvb.lib.dsabLoader.Team;

public class Favorite implements Serializable {

    private static final String NO_STRING = "-";
    private static final int NO_TEAM = -1;

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
