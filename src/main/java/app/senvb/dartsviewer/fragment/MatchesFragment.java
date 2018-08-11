package app.senvb.dartsviewer.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.util.Locale;

import app.senvb.dartsviewer.R;
import app.senvb.dartsviewer.helper.CalendarEntryClickListener;
import senvb.lib.dsabLoader.LeagueData;
import senvb.lib.dsabLoader.Match;


public class MatchesFragment extends Fragment {

    private LeagueData leagueData;
    private TableLayout tableLayout;
    private CalendarEntryClickListener matchCalendarListener;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_matches, container, false);
        TextView title = rootView.findViewById(R.id.leagueTitle);
        leagueData = (LeagueData) getArguments().get("leagueData");
        title.setText(String.format(Locale.getDefault(),"%s - Spiele", leagueData.getName()));
        TextView region = rootView.findViewById(R.id.regionTitle);
        ((TextView) rootView.findViewById(R.id.seasonTitle)).setText(leagueData.getLeagueMetaData().getSeasonName());
        region.setText(leagueData.getLeagueMetaData().getRegionName());
        tableLayout = rootView.findViewById(R.id.matchesList);
        matchCalendarListener = new CalendarEntryClickListener(leagueData, getActivity());
        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handleLeagueData();
    }

    public void handleLeagueData() {
        tableLayout.removeAllViews();
        tableLayout.addView(createHeaderRow());
        for (Match m : leagueData.getMatches().getMatches()) {
            tableLayout.addView(createMatchRow(m, leagueData));
        }
    }

    private TableRow createMatchRow(Match m, LeagueData leagueData) {
        TableRow inflateRow = (TableRow) View.inflate(getActivity(), R.layout.matches_row, null);
        TextView roundGameView = inflateRow.findViewById(R.id.roundGame);
        TextView homeView = inflateRow.findViewById(R.id.home);
        TextView dashView = inflateRow.findViewById(R.id.dash);
        TextView awayView = inflateRow.findViewById(R.id.away);
        TextView dtgsView = inflateRow.findViewById(R.id.dateTimeOrGamesSest);
        roundGameView.setText(String.format(Locale.getDefault(), "%d/%d", m.getRound(), m.getMatchID()));
        roundGameView.setPaintFlags(roundGameView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        roundGameView.setOnClickListener(this.matchCalendarListener);

        awayView.setText(leagueData.getTeamByNumber(m.getAway()).get().getName());
        dashView.setText(" - ");
        homeView.setText(leagueData.getTeamByNumber(m.getHome()).get().getName());
        StringBuilder sb = new StringBuilder();
        if (m.isPlayed()) {
            sb.append(m.getHomeMatches()).append(":").append(m.getAwayMatches()).append(StringUtils.LF);
            sb.append(m.getHomeSets()).append(":").append(m.getAwaySets());
        } else {
            DateFormat dfDate = android.text.format.DateFormat.getDateFormat(getContext());
            DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(getContext());
            sb.append(dfDate.format(m.getDate())).append(StringUtils.LF);
            sb.append(dfTime.format(m.getDate()));
        }
        dtgsView.setText(sb.toString());
        return inflateRow;
    }

    private TableRow createHeaderRow() {
        TableRow inflateRow = (TableRow) View.inflate(getActivity(), R.layout.matches_row, null);
        TextView roundGameView = inflateRow.findViewById(R.id.roundGame);
        TextView homeView = inflateRow.findViewById(R.id.home);
        TextView dashView = inflateRow.findViewById(R.id.dash);
        TextView awayView = inflateRow.findViewById(R.id.away);
        TextView dtgsView = inflateRow.findViewById(R.id.dateTimeOrGamesSest);
        roundGameView.setText(R.string.textRoundNr);
        awayView.setText(R.string.textAway);
        dashView.setText(StringUtils.SPACE);
        homeView.setText(R.string.textHome);
        dtgsView.setText(R.string.textDateResult);
        roundGameView.setTextSize(10.0f);
        awayView.setTextSize(10.0f);
        dashView.setTextSize(10.0f);
        homeView.setTextSize(10.0f);
        dtgsView.setTextSize(10.0f);
        return inflateRow;
    }


}
