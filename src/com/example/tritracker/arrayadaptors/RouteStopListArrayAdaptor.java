package com.example.tritracker.arrayadaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tritracker.R;
import com.example.tritracker.activities.MainService;
import com.example.tritracker.json.AllRoutesJSONResult.ResultSet.Route.Dir.Stop;

import java.util.ArrayList;

public class RouteStopListArrayAdaptor extends ArrayAdapter<Stop> {
	// private final Context context;
	private MainService theService;

	public RouteStopListArrayAdaptor(Context context, ArrayList<Stop> l) {
		super(context, R.layout.fav_stop_layout, l);
		this.theService = MainService.getService();
	}

	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.fav_stop_layout, null);
        }

        Stop curStop = getItem(position);

        if (curStop != null) {
            TextView LineNumber = (TextView) v.findViewById(R.id.StopID);
            TextView LineName = (TextView) v.findViewById(R.id.LineName);


            LineName.setText(curStop.desc);
            LineName.setSelected(true);
            LineNumber.setText(curStop.locid);
        }

        return v;
	}
}
