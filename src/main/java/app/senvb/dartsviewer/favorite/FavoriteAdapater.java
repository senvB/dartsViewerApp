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
