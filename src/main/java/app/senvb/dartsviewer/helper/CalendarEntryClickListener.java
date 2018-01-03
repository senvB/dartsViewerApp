package app.senvb.dartsviewer.helper;

import android.app.Activity;
import android.content.Intent;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;

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
        StringBuilder sb = new StringBuilder();
        sb.append("Darts ").append(leagueData.getName()).append(": ");
        sb.append(leagueData.getTeamByNumber(match.getHome()).getName()).append(" - ").append(leagueData.getTeamByNumber(match.getAway()).getName());
        return sb.toString();
    }

    private String resolveLocationForMatch(Match match) {
        Team homeTeam = leagueData.getTeamByNumber(match.getHome());
        StringBuilder sb = new StringBuilder();
        sb.append(homeTeam.getVenue()).append(", ").append(homeTeam.getAddress());
        return sb.toString();
    }

    private String resolveDescriptionForMatch(Match match) {
        Team homeTeam = leagueData.getTeamByNumber(match.getHome());
        StringBuilder sb = new StringBuilder();
        sb.append("Darts match\n").append(leagueData.getName()).append(", ").append(leagueData.getLeagueMetaData().getSeasonName()).append("\n");
        sb.append(leagueData.getTeamByNumber(match.getHome()).getName()).append(" - ").append(leagueData.getTeamByNumber(match.getAway()).getName()).append("\n");
        sb.append("Match in Runde ").append(match.getRound()).append(", Match ID: ").append(match.getMatchID());
        sb.append("Gaststätte: ").append(homeTeam.getVenue()).append("\n");
        sb.append("Adresse: ").append(homeTeam.getAddress()).append("\n");
        sb.append("Kapitän: ").append(homeTeam.getCaptain()).append("\n");
        sb.append("Telefon: ").append(homeTeam.getPhone());
        return sb.toString();

    }
}
