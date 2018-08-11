package app.senvb.dartsviewer.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import app.senvb.dartsviewer.R;
import app.senvb.dartsviewer.favorite.FavoriteHolder;
import app.senvb.dartsviewer.helper.CalendarEntryClickListener;
import senvb.lib.dsabLoader.LeagueData;
import senvb.lib.dsabLoader.Match;
import senvb.lib.dsabLoader.Player;
import senvb.lib.dsabLoader.Team;


public class TeamFragment extends Fragment {

    private static final String TAG = "TeamFragment";

    private LeagueData leagueData;
    private TableLayout tableLayoutMatches;
    private TableLayout tableLayoutPlayer;
    private Team team;

    private OnClickListener matchCalendarListener;

    private class ButtonClickListener implements OnClickListener {
        public void onClick(View v) {
            Fragment f = new TeamAddressFragment();
            Bundle b = new Bundle();
            b.putString("teamName", team.getName());
            b.putString("teamAddress", team.getAddress());
            b.putString("teamVenue", team.getVenue());
            f.setArguments(b);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, f).addToBackStack(null).commit();
        }
    }

    private class FavoriteClickListener implements OnClickListener {
        public void onClick(View view) {
            if (((CheckBox) view).isChecked()) {
                try {
                    FavoriteHolder.getInstance().addFavorite(leagueData, team);
                    Toast.makeText(getActivity(), "Favorit hinzugefügt", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot add favorite", e);
                }
            } else {
                try {
                    FavoriteHolder.getInstance().removeFavorite(leagueData, team);
                    Toast.makeText(getActivity(), "Favorit gelöscht", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot remove favorite", e);
                }
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team, container, false);
        Bundle b = getArguments();
        this.leagueData = (LeagueData) b.get("leagueData");
        this.team = this.leagueData.getTeamByNumber(b.getInt("teamID")).get();
        ((TextView) rootView.findViewById(R.id.teamTitle)).setText(this.team.getName());
        ((TextView) rootView.findViewById(R.id.teamVenue)).setText(this.team.getVenue());
        TextView address1 = rootView.findViewById(R.id.teamAddress1);
        TextView address2 = rootView.findViewById(R.id.teamAddress2);
        String[] adressString = this.team.getAddress().split(",");
        if (adressString.length > 0) {
            address1.setText(adressString[0].trim());
        } else {
            address1.setText("");
        }
        if (adressString.length > 1) {
            address2.setText(adressString[1].trim());
        } else {
            address2.setText("");
        }
        ((TextView) rootView.findViewById(R.id.teamCaptain)).setText(this.team.getCaptain());
        ((TextView) rootView.findViewById(R.id.teamPhone)).setText(this.team.getPhone());
        rootView.findViewById(R.id.btnShowAddressInMap).setOnClickListener(new ButtonClickListener());
        this.tableLayoutPlayer = rootView.findViewById(R.id.teamPlayer);
        this.tableLayoutMatches = rootView.findViewById(R.id.teamMatches);
        CheckBox favoriteCheck = rootView.findViewById(R.id.favorite);
        favoriteCheck.setOnClickListener(new FavoriteClickListener());
        if (FavoriteHolder.getInstance().isFavoriteTeam(this.leagueData, this.team)) {
            favoriteCheck.setChecked(true);
        }
        matchCalendarListener = new CalendarEntryClickListener(leagueData, getActivity());
        return rootView;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handleLeagueData();
    }

    public void handleLeagueData() {
        this.tableLayoutPlayer.removeAllViews();
        this.tableLayoutPlayer.addView(createHeaderRowPlayer());
        List<Player> teamPlayer = filterPlayer(this.leagueData.getPlayers().getPlayerRanking());

        for (Player p : teamPlayer) {
            this.tableLayoutPlayer.addView(createPlayerRow(p, this.leagueData));
        }
        this.tableLayoutMatches.removeAllViews();
        this.tableLayoutMatches.addView(createHeaderRowMatches());
        for (Match m : this.leagueData.getMatches().getMatches()) {
            if (matchInvolvesTeam(m)) {
                this.tableLayoutMatches.addView(createMatchRow(m, this.leagueData));
            }
        }
    }

    private boolean matchInvolvesTeam(Match m) {
        return m.getAway() == this.team.getTeamID() || m.getHome() == this.team.getTeamID();
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
        StringBuilder sb2 = new StringBuilder();
        if (m.isPlayed()) {
            sb2.append(m.getHomeMatches()).append(":").append(m.getAwayMatches()).append(StringUtils.LF);
            sb2.append(m.getHomeSets()).append(":").append(m.getAwaySets());
        } else {
            DateFormat dfDate = android.text.format.DateFormat.getDateFormat(getContext());
            DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(getContext());
            sb2.append(dfDate.format(m.getDate())).append(StringUtils.LF);
            sb2.append(dfTime.format(m.getDate()));
        }
        dtgsView.setText(sb2.toString());
        return inflateRow;
    }

    private TableRow createHeaderRowMatches() {
        TableRow inflateRow = (TableRow) View.inflate(getActivity(), R.layout.matches_row, null);
        TextView roundGameView = inflateRow.findViewById(R.id.roundGame);
        TextView homeView = inflateRow.findViewById(R.id.home);
        TextView dashView = inflateRow.findViewById(R.id.dash);
        TextView awayView = inflateRow.findViewById(R.id.away);
        TextView dtgsView = inflateRow.findViewById(R.id.dateTimeOrGamesSest);
        roundGameView.setText(R.string.textRoundNr);
        awayView.setText(R.string.textAway);
        dashView.setText(" ");
        homeView.setText(R.string.textHome);
        dtgsView.setText(R.string.textDateResult);
        roundGameView.setTextSize(10.0f);
        awayView.setTextSize(10.0f);
        dashView.setTextSize(10.0f);
        homeView.setTextSize(10.0f);
        dtgsView.setTextSize(10.0f);
        return inflateRow;
    }

    private List<Player> filterPlayer(List<Player> allPlayer) {
        return allPlayer.stream().filter(p -> p.getTeamID() == this.team.getTeamID()).collect(Collectors.toList());
    }

    private TableRow createPlayerRow(Player p, LeagueData leagueData) {
        TableRow inflateRow = (TableRow) View.inflate(getActivity(), R.layout.player_ranking_row, null);
        TextView setNameView = inflateRow.findViewById(R.id.player_name);
        TextView gamesSetsView = inflateRow.findViewById(R.id.player_gamesSets);
        TextView teamView = inflateRow.findViewById(R.id.player_team);
        ((TextView) inflateRow.findViewById(R.id.player_rank)).setText(String.format(Locale.getDefault(), "%d", p.getRank()));
        teamView.setText(leagueData.getTeamByNumber(p.getTeamID()).get().getName());
        StringBuilder sb = new StringBuilder();
        sb.append(p.getGamesPos()).append(":").append(p.getGamesNeg()).append(StringUtils.LF);
        sb.append(p.getSetsPos()).append(":").append(p.getSetsNeg());
        gamesSetsView.setText(sb.toString());
        setNameView.setText(p.getName());
        return inflateRow;
    }

    private TableRow createHeaderRowPlayer() {
        TableRow inflateRow = (TableRow) View.inflate(getActivity(), R.layout.player_ranking_row, null);
        TextView rankView = inflateRow.findViewById(R.id.player_rank);
        TextView setNameView = inflateRow.findViewById(R.id.player_name);
        TextView gamesSetsView = inflateRow.findViewById(R.id.player_gamesSets);
        TextView teamView = inflateRow.findViewById(R.id.player_team);
        rankView.setText(R.string.textRank);
        teamView.setText(R.string.textTeam);
        gamesSetsView.setText(R.string.textGamesSets);
        setNameView.setText(R.string.textName);
        rankView.setTextSize(10.0f);
        teamView.setTextSize(10.0f);
        gamesSetsView.setTextSize(10.0f);
        setNameView.setTextSize(10.0f);
        return inflateRow;
    }
}
