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
package app.senvb.dartsviewer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import app.senvb.dartsviewer.R;
import senvb.lib.dsabLoader.LeagueMetaData;


public class LeagueListAdapter extends ArrayAdapter<LeagueMetaData> {

	private final int resource;

	public LeagueListAdapter(Context context, int res,
			ArrayList<LeagueMetaData> objects) {
		super(context, res, objects);
		this.resource = res;
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		LinearLayout leagueView;
		LeagueMetaData item = getItem(position);
		if (convertView == null) {
			leagueView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li;
			li = (LayoutInflater) getContext().getSystemService(inflater);
			if (li != null) {
				li.inflate(resource, leagueView, true);
			}
		} else {
			leagueView = (LinearLayout) convertView;
		}
		TextView leagueRowView = leagueView
				.findViewById(R.id.leagueRow);
		if (item != null) {
			leagueRowView.setText(item.getName());
		}
		return leagueView;
	}

}
