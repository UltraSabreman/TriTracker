package com.example.tritracker.ArrayAdaptors;

import java.util.ArrayList;

import com.example.tritracker.GlobalData;
import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Util;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StopArrayAdaptor extends ArrayAdapter<Stop> {
	private final ArrayList<Stop> stops;
	private boolean fav = false;

	public StopArrayAdaptor(Context context, ArrayList<Stop> stops,
			boolean favorites) {
		super(context, (favorites ? R.layout.fav_stop_layout
				: R.layout.hist_stop_layout), stops);
		fav = favorites;
		this.stops = stops;
	}

	@Override
	public void notifyDataSetChanged() {
		Util.sortList(fav ? 0 : 1);
		super.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate((fav ? R.layout.fav_stop_layout
					: R.layout.hist_stop_layout), null);
		}

		final Stop curStop = stops.get(position);

		if (curStop != null) {
			if (!fav) {
				final ImageView pic = (ImageView) v.findViewById(R.id.FavIcon);
				if (Util.favHasStop(curStop))
					pic.setImageDrawable(v.getResources().getDrawable(
							R.drawable.ic_action_important_yellow));
				else
					pic.setImageDrawable(v.getResources().getDrawable(
							R.drawable.ic_action_not_important_yellow));

				pic.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (Util.favHasStop(curStop)) {
							pic.setImageDrawable(v.getResources().getDrawable(
									R.drawable.ic_action_not_important_yellow));
							// Util.removeStop(curStop, GlobalData.Favorites);
							GlobalData.favAdaptor.remove(curStop);
						} else {
							pic.setImageDrawable(v.getResources().getDrawable(
									R.drawable.ic_action_important_yellow));
							GlobalData.favAdaptor.add(curStop);
						}
					}
				});
			}

			if (curStop.Alerts != null && curStop.Alerts.size() != 0)
				((ImageView) v.findViewById(R.id.AlertIcon))
						.setVisibility(View.VISIBLE);
			else
				((ImageView) v.findViewById(R.id.AlertIcon))
						.setVisibility(View.INVISIBLE);

			if (curStop.hasNotifications())
				((ImageView) v.findViewById(R.id.ReminderIcon))
						.setVisibility(View.VISIBLE);
			else
				((ImageView) v.findViewById(R.id.ReminderIcon))
						.setVisibility(View.INVISIBLE);

			TextView stopName = (TextView) v.findViewById(R.id.StopName);
			TextView stopID = (TextView) v.findViewById(R.id.StopID);
			TextView lines = (TextView) v.findViewById(R.id.LineNames);

			stopID.setText(String.valueOf(curStop.StopID));
			stopName.setText(curStop.Name);
			stopName.setSelected(true);

			// this lists the routes, and adds commas between them.
			if (curStop.Busses != null && curStop.Busses.size() != 0) {
				lines.setTextColor(Color.parseColor("#aaaaaa"));
				lines.setText("Lines: " + Util.getListOfLines(curStop, true));

			} else {
				lines.setTextColor(Color.parseColor("#FF6666"));
				lines.setText("No Arrival Information");

			}
		}

		return v;
	}
}
