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

import android.app.Activity;
import android.content.Intent;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Optional;

import senvb.lib.dsabLoader.LeagueData;
import senvb.lib.dsabLoader.Match;
import senvb.lib.dsabLoader.Team;


public final class CalendarEntryClickListener implements View.OnClickListener {

    private final LeagueData leagueData;

    private final Activity fragment;

    public CalendarEntryClickListener(LeagueData ld, Activity f) {
        leagueData = ld;
        fragment  = f;
    }

    public void onClick(View v) {
        String matchIdentifier = ((TextView) v).getText().toString().split("/")[1];
        Match match = leagueData.getMatches().getMatchesByID(Integer.parseInt(matchIdentifier));

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, resolveBeginOfMatchInMillis(match))
                .putExtra(CalendarContract.Events.TITLE, resolveTitleForMatch(match))
                .putExtra(CalendarContract.Events.DESCRIPTION, resolveDescriptionForMatch(match))
                .putExtra(CalendarContract.Events.EVENT_LOCATION, resolveLocationForMatch(match))
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_TENTATIVE);
        fragment.startActivity(intent);
    }

    private long resolveBeginOfMatchInMillis(Match match) {
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTime(match.getDate());
        return beginTime.getTimeInMillis();
    }

    private String resolveTitleForMatch(Match match) {
        return "Darts " + leagueData.getName() + ": " + resolveTeamName(match.getHome()) + "" +
                " - " + resolveTeamName(match.getAway());
    }

    private String resolveTeamName(int id) {
        Optional<Team> team = leagueData.getTeamByNumber(id);
        return team.isPresent() ? team.get().getName() : "";
    }

    private String resolveLocationForMatch(Match match) {
        Optional<Team> homeTeam = leagueData.getTeamByNumber(match.getHome());
        StringBuilder sb = new StringBuilder();
        homeTeam.ifPresent(t -> sb.append(t.getVenue()).append(", ").append(t.getAddress()));
        return sb.toString();
    }

    private String resolveDescriptionForMatch(Match match) {
        Optional<Team> homeTeam = leagueData.getTeamByNumber(match.getHome());
        StringBuilder sb = new StringBuilder();
        if (homeTeam.isPresent()) {
            Team home = homeTeam.get();
            sb.append("Darts match\n").append(leagueData.getName()).append(", ").append(leagueData.getLeagueMetaData().getSeasonName()).append("\n");
            sb.append(resolveTeamName(match.getHome())).append(" - ").append(resolveTeamName(match.getAway())).append("\n");
            sb.append("Match in Runde ").append(match.getRound()).append(", Match ID: ").append(match.getMatchID());
            sb.append("Gaststätte: ").append(home.getVenue()).append("\n");
            sb.append("Adresse: ").append(home.getAddress()).append("\n");
            sb.append("Kapitän: ").append(home.getCaptain()).append("\n");
            sb.append("Telefon: ").append(home.getPhone());
        }
        return sb.toString();

    }
}
