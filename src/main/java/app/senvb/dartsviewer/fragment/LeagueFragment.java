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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import app.senvb.dartsviewer.DartsViewerActivity;
import app.senvb.dartsviewer.R;
import app.senvb.dartsviewer.dataCache.DataCache;
import app.senvb.dartsviewer.favorite.FavoriteHolder;
import app.senvb.dartsviewer.task.CurrentTaskHolder;
import app.senvb.dartsviewer.task.LeagueDataDownloadTask;
import senvb.lib.dsabLoader.LeagueData;
import senvb.lib.dsabLoader.LeagueMetaData;
import senvb.lib.dsabLoader.Team;


public class LeagueFragment extends Fragment implements LeagueDataDownloadTask.LeagueDataDownloadHandler {

    private static final String TAG = "LeagueFragment";

    private Button btnMatches;
    private Button btnPlayer;
    private final OnClickListener clickListener;
    private CheckBox favoriteCheck;
    private TextView lastUpdateField;
    private LeagueData leagueData;
    private LeagueMetaData leagueInfo;
    private TableLayout tableLayout;

    private final class ButtonClickListener implements OnClickListener {

        public void onClick(View v) {
            int btnID = v.getId();
            Fragment f = null;
            Bundle b = new Bundle();
            b.putSerializable("leagueData", leagueData);
            if (btnID == R.id.btnLeageDataReload) {
                DataCache.getInstance().removeLeagueDataFromCache(leagueInfo.getRegionID(), leagueInfo.getSeasonID(), leagueInfo.getLeagueID());
                updateLeagueData();
            } else if (btnID == R.id.btnSingleRanking) {
                f = new PlayerRankingFragment();
            } else if (btnID == R.id.btnMatchPlan) {
                f = new MatchesFragment();
            } else if (btnID == R.id.teamName) {
                int tID = leagueData.getTeamByName(((TextView) v).getText().toString()).getTeamID();
                f = new TeamFragment();
                b.putInt("teamID", tID);
            }
            if (f != null) {
                f.setArguments(b);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, f).addToBackStack(null).commit();
            }
        }
    }

    class FavoriteClickListener implements OnClickListener {

        public void onClick(View view) {
            if (((CheckBox) view).isChecked()) {
                try {
                    FavoriteHolder.getInstance().addFavorite(leagueData);
                    Toast.makeText(getActivity(), "Favorit hinzugefügt", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot add favorite", e);
                }
            } else {
                try {
                    FavoriteHolder.getInstance().removeFavorite(leagueData);
                    Toast.makeText(getActivity(), "Favorit gel\u00f6scht", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(TAG, "Cannot remove favorite", e);
                }
            }
        }
    }

    public LeagueFragment() {
        this.clickListener = new ButtonClickListener();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.fragment_league, container, false);
        leagueInfo = (LeagueMetaData) getArguments().get("leagueInfo");
        ((TextView) rootView.findViewById(R.id.teamRankingTitle)).setText(this.leagueInfo.getName());
        TextView regionText = rootView.findViewById(R.id.regionTitle);
        ((TextView) rootView.findViewById(R.id.seasonTitle)).setText(this.leagueInfo.getSeasonName());
        regionText.setText(this.leagueInfo.getRegionName());
        tableLayout = rootView.findViewById(R.id.teamRanking);
        lastUpdateField = rootView.findViewById(R.id.lastUpdateLeagueData);
        rootView.findViewById(R.id.btnMatchPlan).setOnClickListener(this.clickListener);
        rootView.findViewById(R.id.btnLeageDataReload).setOnClickListener(this.clickListener);
        rootView.findViewById(R.id.btnSingleRanking).setOnClickListener(this.clickListener);
        favoriteCheck = rootView.findViewById(R.id.favorite);
        favoriteCheck.setOnClickListener(new FavoriteClickListener());
        if (FavoriteHolder.getInstance().isFavoriteLeague(this.leagueInfo)) {
            favoriteCheck.setChecked(true);
        }
        btnMatches = rootView.findViewById(R.id.btnMatchPlan);
        btnPlayer = rootView.findViewById(R.id.btnSingleRanking);
        return rootView;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateLeagueData();
    }

    private void updateLeagueData() {
        if (((DartsViewerActivity) getActivity()).isNetworkAvailable()) {
            CurrentTaskHolder taskHolder = CurrentTaskHolder.getInstance();
            if (taskHolder.isTaskRunning() && LeagueDataDownloadTask.IDENTIFIER.equals(taskHolder.getIdentifierCurrentTask())) {
                ((LeagueDataDownloadTask) taskHolder.getCurrentTask()).setHandler(this);
            } else {
                LeagueDataDownloadTask task = new LeagueDataDownloadTask((DartsViewerActivity) getActivity(), this);
                CurrentTaskHolder.getInstance().setCurrentTask(task);
                task.execute(leagueInfo);
            }
        } else {
            ((DartsViewerActivity) getActivity()).showNetworkNotAvailableWarning();
        }
    }

    public void handleLeagueData(LeagueData ld) {
        leagueData = ld;
        if (ld != null) {
            tableLayout.removeAllViews();
            tableLayout.addView(createHeaderRow());
            for (Team t : leagueData.getTeams().getTeams()) {
                this.tableLayout.addView(createTeamRow(t));
            }
            Date lastModDate = DataCache.getInstance().getLastUpdateDateForLeagueData(this.leagueInfo.getRegionID(), this.leagueInfo.getSeasonID(), this.leagueInfo.getLeagueID());
            DateFormat dfDate = android.text.format.DateFormat.getDateFormat(getContext());
            DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(getContext());
            this.lastUpdateField.setText(String.format(Locale.getDefault(), "letztes Update: %s, %s", dfDate.format(lastModDate), dfTime.format(lastModDate)));
            return;
        }
        this.btnMatches.setEnabled(false);
        this.btnPlayer.setEnabled(false);
        this.favoriteCheck.setEnabled(false);
        Toast.makeText(getActivity(), "Fehler beim Lesen der Daten. Bitte melden mit Angabe Region, Saison und Liga. Danke", Toast.LENGTH_LONG).show();
    }

    private TableRow createTeamRow(Team t) {
        TableRow inflateRow = (TableRow) View.inflate(getActivity(), R.layout.team_ranking_row, null);
        TextView rankView = inflateRow.findViewById(R.id.rank);
        TextView gamesSetsRatioView = inflateRow.findViewById(R.id.gamesSetsRatio);
        TextView pointsTotalView = inflateRow.findViewById(R.id.pointsTotal);
        TextView gamesTotalView = inflateRow.findViewById(R.id.gamesTotal);
        TextView teamNameView = inflateRow.findViewById(R.id.teamName);
        teamNameView.setPaintFlags(teamNameView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        teamNameView.setOnClickListener(this.clickListener);
        rankView.setText(String.format(Locale.getDefault(), "%d", t.getRank()));
        teamNameView.setText(t.getName());
        gamesTotalView.setText(String.format(Locale.getDefault(), "%d", t.getMatchesPlayed()));
        pointsTotalView.setText(String.format(Locale.getDefault(), "%d", t.getPoints()));
        StringBuilder sb = new StringBuilder();
        sb.append(t.getGamesPos()).append(":").append(t.getGamesNeg()).append(StringUtils.LF);
        sb.append(t.getSetsPos()).append(":").append(t.getSetsNeg());
        gamesSetsRatioView.setText(sb.toString());
        return inflateRow;
    }

    private TableRow createHeaderRow() {
        TableRow inflateRow = (TableRow) View.inflate(getActivity(), R.layout.team_ranking_row, null);
        TextView rankView = inflateRow.findViewById(R.id.rank);
        TextView gamesSetsRatioView = inflateRow.findViewById(R.id.gamesSetsRatio);
        TextView pointsTotalView = inflateRow.findViewById(R.id.pointsTotal);
        TextView gamesTotalView = inflateRow.findViewById(R.id.gamesTotal);
        TextView teamNameView = inflateRow.findViewById(R.id.teamName);
        rankView.setText(R.string.textRank);
        teamNameView.setText(R.string.textTeam);
        gamesTotalView.setText(R.string.textGames);
        pointsTotalView.setText(R.string.textPoints);
        gamesSetsRatioView.setText(R.string.textGamesSets);
        rankView.setTextSize(10.0f);
        teamNameView.setTextSize(10.0f);
        gamesTotalView.setTextSize(10.0f);
        pointsTotalView.setTextSize(10.0f);
        gamesSetsRatioView.setTextSize(10.0f);
        return inflateRow;
    }
}
