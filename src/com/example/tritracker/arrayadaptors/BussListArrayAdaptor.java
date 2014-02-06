package com.example.tritracker.arrayadaptors;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.example.tritracker.Alert;
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

public class BussListArrayAdaptor extends ArrayAdapter<Buss> {
	// private final Context context;
	private Stop curStop;
	private Context context;
	private MainService theService;

	public BussListArrayAdaptor(Context context, ArrayList<Buss> l) {
		super(context, R.layout.buss_layout, l);
		this.context = context;
		this.theService = MainService.getService();
	}

	public void updateStop(Stop s) {
		curStop = s;
	}

	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.buss_layout, null);
		}

		Buss curBuss = getItem(position);

		if (curBuss != null) {
            //Show alert icon?
			ArrayList<Alert> a = theService.getStopAlerts(curStop);
			if (a != null && a.size() != 0) {
				boolean affected = false;
				for (Alert d : a) {
					for (Integer i : d.AffectedLines)
						if (i.intValue() == curBuss.Route) {
							affected = true;
							break;
						}
                    if (affected)
                        break;
				}
				if (affected)
					((ImageView) v.findViewById(R.id.AlertIcon))
							.setVisibility(View.VISIBLE);
				else
					((ImageView) v.findViewById(R.id.AlertIcon))
							.setVisibility(View.INVISIBLE);

			} else
				((ImageView) v.findViewById(R.id.AlertIcon))
						.setVisibility(View.INVISIBLE);

            //show notification info?
            NotificationHandler n = theService.getReminder(curBuss);
            if (n != null && n.IsSet) {
                ((ImageView) v.findViewById(R.id.ReminderIcon)).setVisibility(View.VISIBLE);
                TextView t = (TextView) v.findViewById(R.id.ReminderTime);
                t.setVisibility(View.VISIBLE);
                t.setText(n.getTime() + " Min");
            } else {
                ((ImageView) v.findViewById(R.id.ReminderIcon)).setVisibility(View.INVISIBLE);
                ((TextView) v.findViewById(R.id.ReminderTime)).setVisibility(View.INVISIBLE);
            }

            v = createColoredText(v, curBuss);

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

			String sign = "";
			if (context.getResources().getConfiguration().orientation == 2) // ORIENTATION_LANDSCAPE
				sign = curBuss.SignLong.replace(route + " ", "");
			else
				sign = curBuss.SignShort.replace(route + " ", "");

			if (!Character.isUpperCase(sign.charAt(0)))
				sign = sign.substring(0, 1).toUpperCase(Locale.US) + sign.substring(1);

			LineName.setText(sign);
			LineName.setSelected(true);
		}

		return v;
	}

    public View createColoredText(View v, Buss curBuss) {
        ViewFlipper SFlipper = (ViewFlipper) v.findViewById(R.id.Schedule);
        ViewFlipper TFlipper = (ViewFlipper) v.findViewById(R.id.Time);

        SFlipper.removeAllViews();
        TFlipper.removeAllViews();

        int index = 0;
        Format formatter = new SimpleDateFormat("hh:mm a", Locale.US);
        for(Buss.TimeBox t : curBuss.times) {
            String s = formatter.format(t.ScheduledTime);
            TextView tTextView = new TextView(context);
                tTextView.setTextSize(12);
                tTextView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                tTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                tTextView.setMarqueeRepeatLimit(-1);
                tTextView.setSingleLine(true);
                tTextView.setText("Scheduled at: " + s);
                tTextView.setSelected(true);
            SFlipper.addView(tTextView);


            tTextView = new TextView(context);
                tTextView.setTextSize(16);
                tTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                tTextView.setSingleLine(true);
                tTextView.setMarqueeRepeatLimit(-1);
                tTextView.setGravity(Gravity.CENTER_VERTICAL);

            Date est = null;
            boolean flag = false;
            if (t.Status.compareTo("estimated") == 0) {
                flag = true;
                est = new Date(t.EstimatedTime.getTime()	- new Date().getTime());
            } else
                est = new Date(t.ScheduledTime.getTime()	- new Date().getTime());

            int min = Util.mToS(est.getTime()) / 60;

            String name = "";
            if (min < 30) {
                if (min == 0)
                    name = "Due";
                else
                    name = String.valueOf(min + " Min");
                if (flag) tTextView.setTextColor(context.getResources().getColor(R.color.MinGood));
            } else if (min >= 30 && min < 60) {
                name = String.valueOf(min + " Min");
                if (flag) tTextView.setTextColor(context.getResources().getColor(R.color.MinOk));
            } else {
                Double hours = ((double)min / 60);
                name = new DecimalFormat((hours > 9 ? "0" : "") + "0.0").format(hours) + " Hour" + (hours > 1 ? "s" : "");
                if (flag) tTextView.setTextColor(context.getResources().getColor(R.color.MinBad));
            }
            if (!flag)
                tTextView.setTextColor(context.getResources().getColor(R.color.MinNoGps));

            tTextView.setText(name);
            TFlipper.addView(tTextView);

            index++;
        }

        if (curBuss.times.size() > 1) {
            SFlipper.startFlipping();
            TFlipper.startFlipping();
        }


        return v;
    }

}
