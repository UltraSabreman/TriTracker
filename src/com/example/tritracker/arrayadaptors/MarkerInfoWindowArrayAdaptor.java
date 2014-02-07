package com.example.tritracker.arrayadaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.tritracker.R;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class MarkerInfoWindowArrayAdaptor implements InfoWindowAdapter {
    private LayoutInflater inflater=null;
    private Context context;

    public MarkerInfoWindowArrayAdaptor(LayoutInflater inflater, Context context) {
        this.inflater=inflater;
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return(null);
    }

    @Override
    public View getInfoContents(Marker marker) {
        View popup = inflater.inflate(R.layout.map_marker, null);
        TextView Title = (TextView)popup.findViewById(R.id.title);
        TextView Snippet = (TextView)popup.findViewById(R.id.snippit);
        TextView Lines = (TextView)popup.findViewById(R.id.lines);
        TextView StopID = (TextView)popup.findViewById(R.id.stopID);
        TextView Names = (TextView)popup.findViewById(R.id.names);

        if (marker.getTitle().compareTo("Search Location") == 0) {
            Title.setText(marker.getTitle());
            Title.setSelected(true);
            Title.setTextColor(context.getResources().getColor(R.color.Black));

            Snippet.setText(marker.getSnippet());
            Snippet.setTextColor(context.getResources().getColor(R.color.MediumGrey));
        } else {
            Title.setText(marker.getTitle());
            Title.setSelected(true);

            String data = marker.getSnippet();
            String stopID = data.substring(0,data.indexOf("|"));
            String lines = data.substring(data.indexOf("|") + 1);

            Lines.setText("Line" + (lines.indexOf(",") == -1 ? "" : "s") + ": ");
            Lines.setSelected(true);
            StopID.setText(stopID);
            Names.setText(lines);
            Names.setSelected(true);
        }

        return(popup);
    }
}