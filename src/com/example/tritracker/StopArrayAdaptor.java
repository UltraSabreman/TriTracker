package com.example.tritracker;

import java.util.ArrayList;

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
	public void notifyDataSetChanged() {
		Util.sortList(fav ? 0 : 1);
		super.notifyDataSetChanged();		
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

		final Stop curStop = stops.get(position);

		if (curStop != null) {
			if (!fav) {
				final ImageView pic = (ImageView) v.findViewById(R.id.FavIcon);
				if (Util.favHasStop(curStop))
					pic.setImageDrawable(v.getResources().getDrawable(R.drawable.ic_action_important_yellow));
				else
					pic.setImageDrawable(v.getResources().getDrawable(R.drawable.ic_action_not_important_yellow));
				
				pic.setOnClickListener(new OnClickListener() {
				    @Override
				    public void onClick(View v) {
						if (Util.favHasStop(curStop)) {
							pic.setImageDrawable(v.getResources().getDrawable(R.drawable.ic_action_not_important_yellow));
							Util.removeStop(curStop, GlobalData.Favorites);
						} else {
							pic.setImageDrawable(v.getResources().getDrawable(R.drawable.ic_action_important_yellow));
							GlobalData.Favorites.add(curStop);
						}
				    }
				});
					
			//	((View) v.findViewById(R.id.UIseperator2)).setBackgroundColor(Color.parseColor("#FFF1EE"));
			}

			TextView stopName = (TextView) v.findViewById(R.id.StopName);
			TextView stopID = (TextView) v.findViewById(R.id.StopID);
			TextView lines = (TextView) v.findViewById(R.id.LineNames);

			stopID.setText(String.valueOf(curStop.StopID));
			stopName.setText(curStop.Name);
			stopName.setSelected(true);
			
			//this lists the routes, and adds commas between them.
			if (curStop.Busses != null && curStop.Busses.size() != 0) {
				String s = "";
				int count = 0;
				for (Buss b :  curStop.Busses) {
					String [] words = b.SignLong.split(" ");
					String tempRoute = words[0];
					if (tempRoute.compareTo("MAX") == 0)
						tempRoute = (words[1].isEmpty() ? words[2] : words[1]) + "-Line";
					if (!s.contains(tempRoute)) {
							s+= tempRoute + " ";
							count++;
					}
				}
				lines.setTextColor(Color.parseColor("#aaaaaa"));
				lines.setText((count > 1 ? "Lines: " : "Line: ") + s.replaceAll("( [0-9a-zA-Z])", ",$1"));
				
			}else {
				lines.setTextColor(Color.parseColor("#FF6666"));
				lines.setText("No Arrival Information");
				
			}
		}

		return v;
	}
}
