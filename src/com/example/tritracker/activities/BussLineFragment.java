package com.example.tritracker.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tritracker.R;

public class BussLineFragment extends Fragment {
    private View ourView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ourView = inflater.inflate(R.layout.stop_details_overview, null);


        return ourView;
	}

}
