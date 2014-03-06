package com.example.tritracker.arrayadaptors;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tritracker.R;
import com.example.tritracker.RouteNamer;
import com.example.tritracker.Util;
import com.example.tritracker.activities.MainService;
import com.example.tritracker.json.AllRoutesJSONResult.ResultSet.Route;

import java.util.ArrayList;
import java.util.Locale;

public class RouteListArrayAdaptor extends ArrayAdapter<Route> {
	// private final Context context;
	private MainService theService;
	private Context context;

	public RouteListArrayAdaptor(Context context, ArrayList<Route> l) {
		super(context, R.layout.search_route, l);
		this.context = context;
		this.theService = MainService.getService();
	}


	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.search_route, null);
		}

		Route curRoute = getItem(position);

		if (curRoute != null) {

			TextView LineNumber = (TextView) v.findViewById(R.id.LineNumber);
			LineNumber.setTextColor(Color.BLACK);
			TextView LineName = (TextView) v.findViewById(R.id.LineName);

            LineNumber.setText(RouteNamer.getShortName(curRoute.route));
			String sign = Util.removeRoutePrefix(curRoute.desc, curRoute.route);

            if ((curRoute.desc.contains("MAX") || curRoute.desc.contains("WES") || curRoute.desc.contains("Streetcar") ) && !curRoute.desc.contains("Shuttle"))
                LineNumber.setTextColor(RouteNamer.getColor(curRoute.route));

			if (!Character.isUpperCase(sign.charAt(0)))
				sign = sign.substring(0, 1).toUpperCase(Locale.US) + sign.substring(1);

			LineName.setText(sign);
			LineName.setSelected(true);
		}

		return v;
	}

}
