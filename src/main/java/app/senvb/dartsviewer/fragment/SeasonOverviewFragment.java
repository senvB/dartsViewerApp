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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.senvb.dartsviewer.DartsViewerActivity;
import app.senvb.dartsviewer.R;
import app.senvb.dartsviewer.adapter.SeasonListAdapter;
import app.senvb.dartsviewer.dataCache.DataCache;
import app.senvb.dartsviewer.task.CurrentTaskHolder;
import app.senvb.dartsviewer.task.SeasonOverviewDownloadTask;
import senvb.lib.dsabLoader.Region;
import senvb.lib.dsabLoader.Season;

public class SeasonOverviewFragment extends ListFragment implements SeasonOverviewDownloadTask.SeasonOverviewDownloadHandler {

    private SeasonListAdapter adapter;
    private TextView lastUpdateField;
    private Region region;

    private OnSeasonSelectedListener seasonListener;

    private final class ButtonClickListener implements OnClickListener {
        private ButtonClickListener() {
        }

        public void onClick(View v) {
            if (v.getId() == R.id.btnSeasonReload) {
                DataCache.getInstance().removeSeasonsFromCache(region.getRegionID());
                updateSeasonData();
            }
        }
    }

    public interface OnSeasonSelectedListener {
        void onSeasonSelected(Region region, Season season);
    }

    private class SeasonSorter implements Comparator<Season> {
        public int compare(Season lhs, Season rhs) {
           if (lhs.getStartDate().after(rhs.getStartDate())) {
               return -1;
           } else if (lhs.getStartDate().before(rhs.getStartDate())) {
               return 1;
           }
           return 0;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.fragment_season_overview, container, false);
        region = (Region) getArguments().get("region");
        TextView title = rootView.findViewById(R.id.regionTitle);
        lastUpdateField = rootView.findViewById(R.id.lastUpdateSeason);
        rootView.findViewById(R.id.btnSeasonReload).setOnClickListener(new ButtonClickListener());
        title.setText(region.getRegionName());
        return rootView;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.seasonListener = (OnSeasonSelectedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().getClass().getCanonicalName() + " must implement OnSeasonSelectedListener");
        }
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateSeasonData();
    }

    private void updateSeasonData() {
        if (((DartsViewerActivity) getActivity()).isNetworkAvailable()) {
            CurrentTaskHolder taskHolder = CurrentTaskHolder.getInstance();
            if (taskHolder.isTaskRunning() && SeasonOverviewDownloadTask.IDENTIFIER.equals(taskHolder.getIdentifierCurrentTask())) {
                ((SeasonOverviewDownloadTask) taskHolder.getCurrentTask()).setHandler(this);
            } else {
                SeasonOverviewDownloadTask task = new SeasonOverviewDownloadTask((DartsViewerActivity) getActivity(), this);
                CurrentTaskHolder.getInstance().setCurrentTask(task);
                task.execute(region);
            }
        } else {
            ((DartsViewerActivity) getActivity()).showNetworkNotAvailableWarning();
        }
    }

    private void createAdapter() {
        if (adapter == null) {
            adapter = new SeasonListAdapter(getActivity(), R.layout.season_item, new ArrayList<>());
            setListAdapter(adapter);
        }
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        seasonListener.onSeasonSelected(this.region, (Season) l.getItemAtPosition(position));
    }

    public void handleSeasonOverview(List<Season> seasonData) {
        createAdapter();
        adapter.clear();
        seasonData.sort(new SeasonSorter());
        adapter.addAll(seasonData);
        adapter.notifyDataSetChanged();
        DateFormat dfDate = android.text.format.DateFormat.getDateFormat(getActivity());
        DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(getActivity());
        Date lastModDate = DataCache.getInstance().getLastUpdateDateForSeasons(this.region.getRegionID());
        lastUpdateField.setText(String.format(Locale.getDefault(), "letztes Update: %s, %s", dfDate.format(lastModDate), dfTime.format(lastModDate)));
    }
}
