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
package app.senvb.dartsviewer.fragment;

import android.graphics.Color;
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
import java.util.Optional;

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
    private final OnClickListener clickListener;
    private Button btnMatches;
    private Button btnPlayer;
    private Button btnHomeRanking;
    private Button btnAwayRanking;
    private Button btnFirstHalfRanking;
    private Button btnSecondHalfHalfRanking;
    private Button btnFullRanking;
    private CheckBox favoriteCheck;
    private TextView lastUpdateField;
    private LeagueData leagueData;
    private LeagueMetaData leagueInfo;
    private TableLayout tableLayout;

    private Button currentRanking;

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
        rootView.findViewById(R.id.btnLeageDataReload).setOnClickListener(this.clickListener);
        favoriteCheck = rootView.findViewById(R.id.favorite);
        favoriteCheck.setOnClickListener(new FavoriteClickListener());
        if (FavoriteHolder.getInstance().isFavoriteLeague(this.leagueInfo)) {
            favoriteCheck.setChecked(true);
        }
        btnMatches = rootView.findViewById(R.id.btnMatchPlan);
        btnMatches.setOnClickListener(this.clickListener);
        btnPlayer = rootView.findViewById(R.id.btnSingleRanking);
        btnPlayer.setOnClickListener(this.clickListener);
        btnFullRanking = rootView.findViewById(R.id.btnFullRanking);
        btnFullRanking.setOnClickListener(this.clickListener);
        btnFirstHalfRanking = rootView.findViewById(R.id.btnFirstHalfRanking);
        btnFirstHalfRanking.setOnClickListener(this.clickListener);
        btnSecondHalfHalfRanking = rootView.findViewById(R.id.btnSecondHalfRanking);
        btnSecondHalfHalfRanking.setOnClickListener(this.clickListener);
        btnHomeRanking = rootView.findViewById(R.id.btnHomeRanking);
        btnHomeRanking.setOnClickListener(this.clickListener);
        btnAwayRanking = rootView.findViewById(R.id.btnAwayRanking);
        btnAwayRanking.setOnClickListener(this.clickListener);
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

    private void updateRankingTable(LeagueData.RankingType rt, Button btn) {
        tableLayout.removeAllViews();
        tableLayout.addView(createHeaderRow());
        for (Team t : leagueData.getTeamsWithRanking(rt).getTeams()) {
            tableLayout.addView(createTeamRow(t));
        }
        btn.setBackgroundColor(Color.CYAN);
        if (currentRanking != null) {
            currentRanking.setBackgroundColor(Color.GRAY);
        }
        currentRanking= btn;
    }

    public void handleLeagueData(LeagueData ld) {
        leagueData = ld;
        if (ld != null) {
            updateRankingTable(LeagueData.RankingType.FULL, btnFullRanking);
            Date lastModDate = DataCache.getInstance().getLastUpdateDateForLeagueData(this.leagueInfo.getRegionID(), this.leagueInfo.getSeasonID(), this.leagueInfo.getLeagueID());
            DateFormat dfDate = android.text.format.DateFormat.getDateFormat(getContext());
            DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(getContext());
            lastUpdateField.setText(String.format(Locale.getDefault(), "letztes Update: %s, %s", dfDate.format(lastModDate), dfTime.format(lastModDate)));
            if (!ld.hasPlayers()) {
                Toast.makeText(getActivity(), "Fehler beim Lesen der Spielerdaten von der DSAB Webseite. Diese können aktuell nicht angezeigt werden", Toast.LENGTH_LONG).show();
            }
        } else {
            btnMatches.setEnabled(false);
            btnPlayer.setEnabled(false);
            favoriteCheck.setEnabled(false);
            Toast.makeText(getActivity(), "Fehler beim Lesen der Daten von der DSAB Webseite. Bitte melden mit Angabe Region, Saison und Liga. Danke", Toast.LENGTH_LONG).show();
        }
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
        String sb = t.getGamesPos() + ":" + t.getGamesNeg() + StringUtils.LF + t
                .getSetsPos() + ":" + t.getSetsNeg();
        gamesSetsRatioView.setText(sb);
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
            } else if (btnID == R.id.btnFullRanking) {
                updateRankingTable(LeagueData.RankingType.FULL, btnFullRanking);
            } else if (btnID == R.id.btnHomeRanking) {
                updateRankingTable(LeagueData.RankingType.HOME, btnHomeRanking);
            } else if (btnID == R.id.btnAwayRanking) {
                updateRankingTable(LeagueData.RankingType.AWAY, btnAwayRanking);
            } else if (btnID == R.id.btnFirstHalfRanking) {
                updateRankingTable(LeagueData.RankingType.FIRST_HALF, btnFirstHalfRanking);
            } else if (btnID == R.id.btnSecondHalfRanking) {
                updateRankingTable(LeagueData.RankingType.SECOND_HALF, btnSecondHalfHalfRanking);
            } else if (btnID == R.id.btnMatchPlan) {
                f = new MatchesFragment();
            } else if (btnID == R.id.teamName) {
                Optional<Team> tm = leagueData.getTeamByName(((TextView) v).getText().toString());
                if (tm.isPresent()) {
                    int tID = tm.get().getTeamID();
                    f = new TeamFragment();
                    b.putInt("teamID", tID);
                } else {
                    Log.e(TAG, "Cannot resolve team from its name.");
                }
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
}
