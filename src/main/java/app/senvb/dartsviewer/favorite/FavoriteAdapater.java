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
package app.senvb.dartsviewer.favorite;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import app.senvb.dartsviewer.R;

public class FavoriteAdapater extends ArrayAdapter<Favorite> {

    public FavoriteAdapater(Context context, int resource, List<Favorite> objects) {
        super(context, resource, objects);
    }

    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position);
    }

    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position);
    }

    private View getCustomView(int position) {
        Favorite item = getItem(position);
        View row = View.inflate(getContext(), R.layout.favorite_item, null);
        TextView nameView = row.findViewById(R.id.favoriteName);
        TextView regionView = row.findViewById(R.id.favoriteRegionSeason);
        if (item != null) {
            nameView.setText(item.getFavoriteName());
            String regionText = item.getRegionName() + ", " + item.getSeasonName();
            regionView.setText(regionText);
        }
        return row;
    }
}
