package com.example.tritracker.arrayadaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tritracker.Buss;
import com.example.tritracker.NotificationHandler;
import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Util;
import com.example.tritracker.activities.MainService;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BussOverviewSpinnerAdaptor extends ArrayAdapter<Buss.TimeBox> {
	// private final Context context;
	private Stop curStop;
	private Buss curBuss;
	private Context context;
	private MainService theService;

	public BussOverviewSpinnerAdaptor(Context context, Stop s, Buss b, ArrayList<Buss.TimeBox> l) {
		super(context, R.layout.stopdetails_overview_line, l);
		this.context = context;
		this.curStop = s;
		this.curBuss = b;
		this.theService = MainService.getService();
	}

	@Override
	public View getDropDownView(int pos, final View v, ViewGroup p) {
		return getView(pos, v, p);
	}

	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.stopdetails_overview_line, null);
		}

		Buss.TimeBox curBox = getItem(position);

		if (curBox != null) {
			if (theService.doesBussHaveAlerts(curBuss))
				((ImageView) v.findViewById(R.id.AlertIcon)).setVisibility(View.VISIBLE);
			else
				((ImageView) v.findViewById(R.id.AlertIcon)).setVisibility(View.INVISIBLE);


			//show notification info?
			NotificationHandler n = theService.getReminder(curBox);
			if (n != null && n.IsSet) {
				((ImageView) v.findViewById(R.id.ReminderIcon)).setVisibility(View.VISIBLE);
				TextView t = (TextView) v.findViewById(R.id.ReminderTime);
				t.setVisibility(View.VISIBLE);
				t.setText(n.getTime() + " Min");
			} else {
				((ImageView) v.findViewById(R.id.ReminderIcon)).setVisibility(View.INVISIBLE);
				((TextView) v.findViewById(R.id.ReminderTime)).setVisibility(View.INVISIBLE);
			}

			v = createColoredText(v, curBox);

			TextView LineNumber = (TextView) v.findViewById(R.id.LineNumber);
			TextView LineName = (TextView) v.findViewById(R.id.LineName);

			//Set the route name
			String route = "";
			String name = curBuss.SignLong;

			if (name.contains(String.valueOf(curBuss.Route))) {
				route = String.valueOf(curBuss.Route);
			} else if (name.contains("WES")) {
				route = "WES";
			} else if (name.contains("Streetcar")) {
				route = "PSC";
			} else if (name.contains("Tram")) {
				route = "TRM";
			} else if (name.contains("Trolley")) {
				route = "TRL";
			} else {
				route = "MAX";
				if (name.contains("Green"))
					LineNumber.setTextColor(context.getResources().getColor(R.color.MaxGreen)); //green
				else if (name.contains("Red"))
					LineNumber.setTextColor(context.getResources().getColor(R.color.MaxRed));
				else if (name.contains("Blue"))
					LineNumber.setTextColor(context.getResources().getColor(R.color.MaxBlue));
				else if (name.contains("Yellow"))
					LineNumber.setTextColor(context.getResources().getColor(R.color.MaxYellow));
			}

			LineNumber.setText(route);

			String sign = curBuss.SignShort.replace(route + " ", "");

			if (!Character.isUpperCase(sign.charAt(0)))
				sign = sign.substring(0, 1).toUpperCase(Locale.US) + sign.substring(1);

			LineName.setText(sign);
			LineName.setSelected(true);
		}

		return v;
	}

	public View createColoredText(View v, Buss.TimeBox curBox) {
		TextView Schedule = (TextView) v.findViewById(R.id.Schedule);
		TextView Time = (TextView) v.findViewById(R.id.Time);


		int index = 0;
		Format formatter = new SimpleDateFormat("hh:mm a", Locale.US);
		Schedule.setText("Scheduled at: " + formatter.format(curBox.ScheduledTime));


		Date est = null;
		boolean flag = false;
		if (curBox.Status.compareTo("estimated") == 0) {
			flag = true;
			est = new Date(curBox.EstimatedTime.getTime() - new Date().getTime());
		} else
			est = new Date(curBox.ScheduledTime.getTime() - new Date().getTime());

		int min = Util.mToS(est.getTime()) / 60;

		String name = "";
		if (min < 30) {
			if (min == 0)
				name = "Due";
			else
				name = String.valueOf(min + " Min");
			if (flag) Time.setTextColor(context.getResources().getColor(R.color.MinGood));
		} else if (min >= 30 && min < 60) {
			name = String.valueOf(min + " Min");
			if (flag) Time.setTextColor(context.getResources().getColor(R.color.MinOk));
		} else {
			Double hours = ((double) min / 60);
			name = new DecimalFormat((hours > 9 ? "0" : "") + "0.0").format(hours) + " Hour" + (hours > 1 ? "s" : "");
			if (flag) Time.setTextColor(context.getResources().getColor(R.color.MinBad));
		}
		if (!flag)
			Time.setTextColor(context.getResources().getColor(R.color.MinNoGps));

		Time.setText(name);

		return v;
	}

}
