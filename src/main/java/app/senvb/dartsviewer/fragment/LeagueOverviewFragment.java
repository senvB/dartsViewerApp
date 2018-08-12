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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.senvb.dartsviewer.DartsViewerActivity;
import app.senvb.dartsviewer.R;
import app.senvb.dartsviewer.adapter.LeagueListAdapter;
import app.senvb.dartsviewer.dataCache.DataCache;
import app.senvb.dartsviewer.task.CurrentTaskHolder;
import app.senvb.dartsviewer.task.LeagueOverviewDownloadTask;
import senvb.lib.dsabLoader.LeagueMetaData;
import senvb.lib.dsabLoader.Region;
import senvb.lib.dsabLoader.Season;


public class LeagueOverviewFragment extends ListFragment implements LeagueOverviewDownloadTask.LeagueOverviewDownloadHandler {

    private LeagueListAdapter adapter;
    private TextView lastUpdateField;
    private OnLeagueSelectedListener leagueListener;
    private Region region;
    private Season season;

    private final class ButtonClickListener implements OnClickListener {

        public void onClick(View v) {
            if (v.getId() == R.id.btnLeageOverviewReload) {
                DataCache.getInstance().removeLeagueMetaDataFromCache(season.getSeasonID());
                updateLeagueOverview();
            }
        }
    }

    public interface OnLeagueSelectedListener {
        void onLeagueSelected(LeagueMetaData leagueMetaData, Season season, Region region);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.fragment_leagues_overview, container, false);
        TextView title = rootView.findViewById(R.id.seasonTitle);
        TextView regionView = rootView.findViewById(R.id.regionTitle);
        Bundle args = getArguments();
        season = (Season) args.get("season");
        region = (Region) args.get("region");
        title.setText(season.getName());
        regionView.setText(region.getRegionName());
        lastUpdateField = rootView.findViewById(R.id.lastUpdateLeagueOverview);
        rootView.findViewById(R.id.btnLeageOverviewReload).setOnClickListener(new ButtonClickListener());
        return rootView;
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        leagueListener.onLeagueSelected((LeagueMetaData) l.getItemAtPosition(position), this.season, this.region);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.leagueListener = (OnLeagueSelectedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().getClass().getCanonicalName() + " must implement OnLeagueSelectedListener");
        }
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateLeagueOverview();
    }

    private void updateLeagueOverview() {
        DartsViewerActivity act = (DartsViewerActivity) getActivity();
        if (act.isNetworkAvailable()) {
            CurrentTaskHolder taskHolder = CurrentTaskHolder.getInstance();
            if (taskHolder.isTaskRunning() && LeagueOverviewDownloadTask.IDENTIFIER.equals(taskHolder.getIdentifierCurrentTask())) {
                ((LeagueOverviewDownloadTask) taskHolder.getCurrentTask()).setHandler(this);
            } else {
                LeagueOverviewDownloadTask task = new LeagueOverviewDownloadTask((DartsViewerActivity) getActivity(), this);
                CurrentTaskHolder.getInstance().setCurrentTask(task);
                task.execute(season);
            }
        } else {
            act.showNetworkNotAvailableWarning();
        }
    }

    private void createAdapter() {
        if (adapter == null) {
            adapter = new LeagueListAdapter(getActivity(), R.layout.league_item, new ArrayList<>());
            setListAdapter(adapter);
        }
    }

    public void handleLeagueOverview(List<LeagueMetaData> leagueData) {
        if (leagueData != null) {
            createAdapter();
            adapter.clear();
            adapter.addAll(leagueData);
            Date lastModDate = DataCache.getInstance().getLastUpdateDateForLeagueMetaData(season.getSeasonID());
            lastUpdateField.setText(String.format(Locale.getDefault(), "letztes Update: %s, %s", DateFormat.getDateFormat(getActivity()).format(lastModDate), DateFormat.getTimeFormat(getActivity()).format(lastModDate)));
            adapter.notifyDataSetChanged();
        }
    }
}
