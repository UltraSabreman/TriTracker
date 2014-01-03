package com.example.tritracker;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BussArrayAdaptor extends ArrayAdapter<Buss> {
	// private final Context context;
	private final ArrayList<Buss> busses;

	public BussArrayAdaptor(Context context, ArrayList<Buss> busses) {
		super(context, R.layout.buss_layout, busses);
		// this.context = context;
		this.busses = busses;
	}

	@Override
	public void notifyDataSetChanged() {
		Util.sortList(2);
		super.notifyDataSetChanged();
		
	}
	@SuppressWarnings("deprecation")
	@SuppressLint("SimpleDateFormat")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.buss_layout, null);
		}

		Buss curBuss = busses.get(position);

		if (curBuss != null) {
			TextView LineNumber = (TextView) v.findViewById(R.id.LineNumber);
			TextView LineName = (TextView) v.findViewById(R.id.LineName);
			TextView Shedule = (TextView) v.findViewById(R.id.Schedule);
			TextView Time = (TextView) v.findViewById(R.id.Time);
			
			if(curBuss.notification != null && curBuss.notification.IsSet) {
				((ImageView) v.findViewById(R.id.ReminderIcon)).setVisibility(View.VISIBLE);
			}else
				((ImageView) v.findViewById(R.id.ReminderIcon)).setVisibility(View.INVISIBLE);

			String route = "";
			if (curBuss.SignShort.contains(String.valueOf(curBuss.Route))) 
				route = String.valueOf(curBuss.Route);
			else
				if (curBuss.SignShort.contains("WES"))
					route = "WES";
				else
					route = "MAX";
			
			LineNumber.setText(route);
			
			String sign = "";
			if (GlobalData.Orientation == 2 ) //ORIENTATION_LANDSCAPE
				sign = curBuss.SignLong.replace(route + " ", "");
			else
				sign = curBuss.SignShort.replace(route + " ", "");
			
			if (Character.isUpperCase(sign.charAt(0)))
				LineName.setText(sign);
			else
				sign = sign.substring(0, 1).toUpperCase() + sign.substring(1);
			
			LineName.setSelected(true);
			

			Format formatter = new SimpleDateFormat("hh:mm a");
			String s = formatter.format(curBuss.ScheduledTime);

			Shedule.setText("Scheduled at: " + s);
			
			if (curBuss.Status.compareTo("estimated") == 0) {
				Date est = new Date(curBuss.EstimatedTime.getTime()	- new Date().getTime());
				int min = est.getMinutes();
				
				if (min < 30) {
					if (min == 0)
						s = "Due";
					else {
						formatter = new SimpleDateFormat("mm");
						s = (formatter.format(est) + " Min");
						if (min < 10)
							s = s.replaceFirst("0", "");
					}
					Time.setTextColor(Color.parseColor("#5CC439"));
				} else if (min >= 30 && min < 60) {
					formatter = new SimpleDateFormat("mm");
					s = formatter.format(est) + " Min";
					Time.setTextColor(Color.parseColor("#FACF11"));
				} else {
					formatter = new SimpleDateFormat("h");
					s = formatter.format(est) + " Hour(s)";
					Time.setTextColor(Color.parseColor("#ED4040"));
				}
				Time.setText(s);
			} else {
				Time.setTextColor(Color.parseColor("#000000"));
				Time.setText("No GPS");
			}

		}

		return v;
	}
}
