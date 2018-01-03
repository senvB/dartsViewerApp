package app.senvb.dartsviewer.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.List;

import app.senvb.dartsviewer.R;
import senvb.lib.dsabLoader.Season;


public class SeasonListAdapter extends ArrayAdapter<Season> {

	private final int resource;

	public SeasonListAdapter(Context context, int res,
			List<Season> objects) {
		super(context, res, objects);
		resource = res;
	}

	@NonNull
    @Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		LinearLayout seasonView;
		Season item = getItem(position);
		if (convertView == null) {
			seasonView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li;
			li = (LayoutInflater) getContext().getSystemService(inflater);
			if (li != null) {
				li.inflate(resource, seasonView, true);
			}
		} else {
			seasonView = (LinearLayout) convertView;
		}
		TextView dateTextView = seasonView
				.findViewById(R.id.rowDate);
		TextView seasonTextView = seasonView.findViewById(R.id.row);
		if (item != null) {
			StringBuilder sb = new StringBuilder();
			DateFormat dfDate = android.text.format.DateFormat.getDateFormat(getContext());
			sb.append(dfDate.format(item.getStartDate())).append(" - ").append(dfDate.format(item.getEndDate()));
			dateTextView.setText(sb.toString());
			seasonTextView.setText(item.getName());
		}
		return seasonView;
	}

}
