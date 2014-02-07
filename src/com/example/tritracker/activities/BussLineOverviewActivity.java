package com.example.tritracker.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;

import com.example.tritracker.R;

public class BussLineOverviewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, LayoutParams.FLAG_NOT_TOUCH_MODAL);
		setContentView(R.layout.stop_details_overview_line);



	}

}
