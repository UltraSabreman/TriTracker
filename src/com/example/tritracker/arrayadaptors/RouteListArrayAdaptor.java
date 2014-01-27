package com.example.tritracker.arrayadaptors;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.activities.MainService;
import com.example.tritracker.json.AllRoutesJSONResult.ResultSet.Route;

public class RouteListArrayAdaptor extends ArrayAdapter<Route> {
	// private final Context context;
	private MainService theService;

	public RouteListArrayAdaptor(Context context, ArrayList<Route> l) {
		super(context, R.layout.buss_layout, l);
		this.theService = MainService.getService();
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();

	}
	
	public void updateStop(Stop s) {
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.route_layout, null);
		}

		Route curRoute = getItem(position);

		if (curRoute != null) {
			if (theService.routeHasAlert(curRoute.route)) 
				((ImageView) v.findViewById(R.id.AlertIcon)).setVisibility(View.VISIBLE);
			else
				((ImageView) v.findViewById(R.id.AlertIcon)).setVisibility(View.INVISIBLE);

			TextView LineNumber = (TextView) v.findViewById(R.id.LineNumber);
			LineNumber.setTextColor(Color.BLACK);
			TextView LineName = (TextView) v.findViewById(R.id.LineName);
			
			String route = "";
			String sign = "";
			if (curRoute.desc.contains(String.valueOf(curRoute.route))) {
				route = String.valueOf(curRoute.route);
				sign = curRoute.desc.replace(route + "-", "");
			} else if (curRoute.desc.contains("WES")) {
				route = "WES";
				sign = curRoute.desc.replace(route + " ", "");
			} else if (curRoute.desc.contains("Streetcar")) {
				route = "PSC";
				sign = curRoute.desc;
			} else if (curRoute.desc.contains("Tram")) {
				route = "TRAM";
				sign = curRoute.desc;
			} else {
				route = "MAX";
				sign = curRoute.desc.replace(route + " ", "");
				if (curRoute.desc.contains("Green"))
					LineNumber.setTextColor(Color.parseColor("#73E673")); //green
				else if (curRoute.desc.contains("Red"))
					LineNumber.setTextColor(Color.parseColor("#E67375"));
				else if (curRoute.desc.contains("Blue"))
					LineNumber.setTextColor(Color.parseColor("#7399E6"));
				else if (curRoute.desc.contains("Yellow"))
					LineNumber.setTextColor(Color.parseColor("#E6DC73"));
			}

			LineNumber.setText(route);
			
			if (!Character.isUpperCase(sign.charAt(0)))
				sign = sign.substring(0, 1).toUpperCase(Locale.US) + sign.substring(1);

			LineName.setText(sign);
			LineName.setSelected(true);
		}

		return v;
	}
}
