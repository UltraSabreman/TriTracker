package com.example.tritracker.arrayadaptors;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tritracker.R;
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
                route = "TRM";
                sign = curRoute.desc;
            } else if (curRoute.desc.contains("Trolley")) {
                route = "TRL";
                sign = curRoute.desc;
            } else {
                route = "MAX";
                sign = curRoute.desc.replace(route + " ", "");
                if (curRoute.desc.contains("Green"))
                    LineNumber.setTextColor(context.getResources().getColor(R.color.MaxGreen)); //green
                else if (curRoute.desc.contains("Red"))
                    LineNumber.setTextColor(context.getResources().getColor(R.color.MaxRed));
                else if (curRoute.desc.contains("Blue"))
                    LineNumber.setTextColor(context.getResources().getColor(R.color.MaxBlue));
                else if (curRoute.desc.contains("Yellow"))
                    LineNumber.setTextColor(context.getResources().getColor(R.color.MaxYellow));
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
