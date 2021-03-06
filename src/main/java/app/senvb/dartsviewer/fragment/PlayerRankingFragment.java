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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

import app.senvb.dartsviewer.R;
import app.senvb.dartsviewer.helper.TeamResolver;
import senvb.lib.dsabLoader.LeagueData;
import senvb.lib.dsabLoader.Player;


public class PlayerRankingFragment extends Fragment {

    private static final String TAG = "PlayerRankingFragment";

    private LeagueData leagueData;
    private TableLayout tableLayout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_league_player_ranking, container, false);
        leagueData = (LeagueData) getArguments().get("leagueData");
        if (leagueData != null) {
            String rankingTitle = leagueData.getName() + " - Einzelrangliste";
            ((TextView) rootView.findViewById(R.id.teamRankingTitle)).setText(rankingTitle);
            TextView region = rootView.findViewById(R.id.regionTitle);
            ((TextView) rootView.findViewById(R.id.seasonTitle)).setText(leagueData.getLeagueMetaData().getSeasonName());
            region.setText(leagueData.getLeagueMetaData().getRegionName());
        } else {
            Log.e(TAG, "Cannot read league data information from arguments");
        }
        tableLayout = rootView.findViewById(R.id.playerRanking);
        return rootView;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handleLeagueData();
    }

    private void handleLeagueData() {
        tableLayout.removeAllViews();
        tableLayout.addView(createHeaderRow());
        for (Player p : leagueData.getPlayers().getPlayerRanking()) {
            tableLayout.addView(createPlayerRow(p, leagueData));
        }
    }



    private TableRow createPlayerRow(Player p, LeagueData leagueData) {
        TableRow inflateRow = (TableRow) View.inflate(getActivity(), R.layout.player_ranking_row, null);
        TextView setNameView = inflateRow.findViewById(R.id.player_name);
        TextView gamesSetsView = inflateRow.findViewById(R.id.player_gamesSets);
        TextView teamView = inflateRow.findViewById(R.id.player_team);
        ((TextView) inflateRow.findViewById(R.id.player_rank)).setText(String.format(Locale.getDefault(), "%d", p.getRank()));
        teamView.setText(TeamResolver.resolveTeamName(p.getTeamID(), leagueData));
        String sb = p.getGamesPos() + ":" + p.getGamesNeg() + StringUtils.LF + p
                .getSetsPos() + ":" + p.getSetsNeg();
        gamesSetsView.setText(sb);
        setNameView.setText(p.getName());
        return inflateRow;
    }

    private TableRow createHeaderRow() {
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
