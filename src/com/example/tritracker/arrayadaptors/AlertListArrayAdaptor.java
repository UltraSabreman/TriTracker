package com.example.tritracker.arrayadaptors;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tritracker.Alert;
import com.example.tritracker.R;

public class AlertListArrayAdaptor extends ArrayAdapter<Alert> {
	// private final Context context;
	private final ArrayList<Alert> alerts;

	public AlertListArrayAdaptor(Context context, ArrayList<Alert> alerts) {
		super(context, R.layout.alert_layout, alerts);
		// this.context = context;
		this.alerts = alerts;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.alert_layout, null);
		}

		Alert curAlert = alerts.get(position);

		if (curAlert != null) {
			TextView lines = (TextView) v.findViewById(R.id.AlertLines);
			TextView disc = (TextView) v.findViewById(R.id.AlertDiscritpion);

			String al = "";
			for (Integer i :curAlert.AffectedLines)
				al += String.valueOf(i) + ", ";
			
			if (al.length() > 2)
				al = al.substring(0, al.length() - 2);
			lines.setText(" " + al);
			
			disc.setText(curAlert.Discription);
		}

		return v;
	}
}
