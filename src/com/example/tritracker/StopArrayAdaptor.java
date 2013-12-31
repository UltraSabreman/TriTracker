package com.example.tritracker;

import java.util.ArrayList;

import com.example.tritracker.R.color;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StopArrayAdaptor extends ArrayAdapter<Stop> {
	// private final Context context;
	private final ArrayList<Stop> stops;
	private boolean fav = false;

	public StopArrayAdaptor(Context context, ArrayList<Stop> stops, boolean favorites) {
		super(context, (favorites ? R.layout.fav_stop_layout : R.layout.hist_stop_layout), stops);
		fav = favorites;
		// this.context = context;
		this.stops = stops;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate((fav ? R.layout.fav_stop_layout : R.layout.hist_stop_layout), null);
		}

		Stop curStop = stops.get(position);

		if (curStop != null) {
			TextView stopName = (TextView) v.findViewById(R.id.StopName);
			TextView stopID = (TextView) v.findViewById(R.id.StopID);
			TextView lines = (TextView) v.findViewById(R.id.LineNames);

			stopID.setText(String.valueOf(curStop.StopID));
			stopName.setText(curStop.Name);
			stopName.setSelected(true);
			
			//this lists the routes, and adds commas between them.
			if (curStop.Busses != null && curStop.Busses.size() != 0) {
				String s = "";
				for (Buss b :  curStop.Busses)
					s += (s.contains(String.valueOf(b.Route)) ? "" : b.Route + " ");
				lines.setTextColor(Color.parseColor("#aaaaaa"));
				lines.setText("Lines: " + s.replaceAll("( [0-9])", ",$1"));
				
			}else {
				lines.setTextColor(Color.parseColor("#FF6666"));
				lines.setText("No Arrival Information");
				
			}
		}

		return v;
	}
}
