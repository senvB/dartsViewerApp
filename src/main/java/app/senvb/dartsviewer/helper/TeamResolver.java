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
package app.senvb.dartsviewer.helper;

import java.util.Optional;

import senvb.lib.dsabLoader.LeagueData;
import senvb.lib.dsabLoader.Team;

public class TeamResolver {

    public static String resolveTeamName(int id, LeagueData leagueData) {
        Optional<Team> team = leagueData.getTeamByNumber(id);
        return team.isPresent() ? team.get().getName() : "";
    }
}
