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
import senvb.lib.dsabLoader.Region;


public class RegionListAdapter extends ArrayAdapter<Region> {

	private final int resource;

	public RegionListAdapter(Context context, int res,
			ArrayList<Region> objects) {
		super(context, res, objects);
		this.resource = res;
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		LinearLayout regionView;
		Region item = getItem(position);
		if (convertView == null) {
			regionView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li;
			li = (LayoutInflater) getContext().getSystemService(inflater);
			if (li != null) {
				li.inflate(resource, regionView, true);
			}
		} else {
			regionView = (LinearLayout) convertView;
		}
		TextView nameView = regionView
				.findViewById(R.id.regionName);
		TextView managerView = regionView.findViewById(R.id.regionManager);
		TextView homepageView = regionView.findViewById(R.id.regionHomepage);
        TextView emailView = regionView.findViewById(R.id.regionEmail);
		if (item != null) {
			nameView.setText(item.getRegionName());
			emailView.setText(item.getRegionManagerEmail());
			managerView.setText(item.getRegionManager());
			homepageView.setText(item.getHomepage());
		}
		return regionView;
	}

}
