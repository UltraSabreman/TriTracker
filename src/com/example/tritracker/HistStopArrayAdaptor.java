package com.example.tritracker;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HistStopArrayAdaptor extends ArrayAdapter<Stop> {
  //private final Context context;
  private final ArrayList<Stop> stops;

  public HistStopArrayAdaptor(Context context, ArrayList<Stop> stops) {
    super(context, R.layout.hist_stop_layout, stops);
    //this.context = context;
    this.stops = stops;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

	// assign the view we are converting to a local variable
	View v = convertView;

	// first check to see if the view is null. if so, we have to inflate it.
	// to inflate it basically means to render, or show, the view.
	if (v == null) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflater.inflate(R.layout.hist_stop_layout, null);
	}

	Stop curStop = stops.get(position);

	if (curStop != null) {
	    TextView stopName = (TextView) v.findViewById(R.id.StopName);
	    TextView stopID = (TextView) v.findViewById(R.id.StopID);
	    
	    stopID.setText(String.valueOf(curStop.StopID));
	    stopID.setTextColor(Color.parseColor("#919191"));
	    stopName.setText(curStop.Name);
	    
	}

    
    return  v;
  }
} 
