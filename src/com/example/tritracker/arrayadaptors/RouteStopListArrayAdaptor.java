package com.example.tritracker.arrayadaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tritracker.R;
import com.example.tritracker.activities.MainService;
import com.example.tritracker.json.AllRoutesJSONResult.ResultSet.Route.Dir.Stop;

import java.util.ArrayList;

public class RouteStopListArrayAdaptor extends ArrayAdapter<Stop> {
	// private final Context context;
	private MainService theService;

	public RouteStopListArrayAdaptor(Context context, ArrayList<Stop> l) {
		super(context, R.layout.main_stoplist_favorites, l);
		this.theService = MainService.getService();
	}

	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.main_stoplist_favorites, null);
		}

		Stop curStop = getItem(position);

		if (curStop != null) {
			TextView LineNumber = (TextView) v.findViewById(R.id.StopID);
			TextView LineName = (TextView) v.findViewById(R.id.StopName);


			LineName.setText(curStop.desc);
			LineName.setSelected(true);

			LineNumber.setText(String.valueOf(curStop.locid));

			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) LineNumber.getLayoutParams();
			lp.addRule(RelativeLayout.CENTER_VERTICAL);
			LineNumber.setLayoutParams(lp);

			lp = (RelativeLayout.LayoutParams) LineName.getLayoutParams();
			lp.addRule(RelativeLayout.CENTER_VERTICAL);
			LineName.setLayoutParams(lp);
		}

		return v;
	}
}
