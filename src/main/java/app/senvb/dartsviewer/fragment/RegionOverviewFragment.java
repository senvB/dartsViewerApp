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

import java.util.ArrayList;
import java.util.List;

import app.senvb.dartsviewer.DartsViewerActivity;
import app.senvb.dartsviewer.R;
import app.senvb.dartsviewer.adapter.RegionListAdapter;
import app.senvb.dartsviewer.dataCache.DataCache;
import app.senvb.dartsviewer.task.CurrentTaskHolder;
import app.senvb.dartsviewer.task.RegionOverviewDownloadTask;
import senvb.lib.dsabLoader.Region;


public class RegionOverviewFragment extends ListFragment implements RegionOverviewDownloadTask.RegionOverviewDownloadHandler {

    private RegionListAdapter adapter;
    private OnRegionSelectedListener regionListener;
    private String stateName;

    private final class ButtonClickListener implements OnClickListener {

        public void onClick(View v) {
            if (v.getId() == R.id.btnRegionReload) {
                DataCache.getInstance().removeRegionFromCache(stateName);
                updateRegionData();
            }
        }
    }

    public interface OnRegionSelectedListener {
        void onRegionSelected(Region groupOfLeagues);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.fragment_region_overview, container, false);
        stateName = getArguments().getString("state");
        TextView title = rootView.findViewById(R.id.stateTitle);
        rootView.findViewById(R.id.btnRegionReload).setOnClickListener(new ButtonClickListener());
        title.setText(stateName);
        return rootView;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.regionListener = (OnRegionSelectedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().getClass().getCanonicalName() + " must implement OnSeasonSelectedListener");
        }
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateRegionData();
    }

    private void updateRegionData() {
        if (((DartsViewerActivity) getActivity()).isNetworkAvailable()) {
            CurrentTaskHolder taskHolder = CurrentTaskHolder.getInstance();
            if (taskHolder.isTaskRunning() && RegionOverviewDownloadTask.IDENTIFIER.equals(taskHolder.getIdentifierCurrentTask())) {
                ((RegionOverviewDownloadTask) taskHolder.getCurrentTask()).setHandler(this);
            } else {
                RegionOverviewDownloadTask task = new RegionOverviewDownloadTask((DartsViewerActivity) getActivity(), this);
                CurrentTaskHolder.getInstance().setCurrentTask(task);
                task.execute(stateName);
            }
        } else {
            ((DartsViewerActivity) getActivity()).showNetworkNotAvailableWarning();
        }
    }

    private void createAdapter() {
        if (adapter == null) {
            adapter = new RegionListAdapter(getActivity(), R.layout.region_item, new ArrayList<>());
            setListAdapter(adapter);
        }
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        regionListener.onRegionSelected((Region) l.getItemAtPosition(position));
    }

    public void handleRegionOverview(List<Region> regionData) {
        createAdapter();
        adapter.clear();
        adapter.addAll(regionData);
        adapter.notifyDataSetChanged();
    }
}
