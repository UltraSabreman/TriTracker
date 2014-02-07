package com.example.tritracker.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;

import com.example.tritracker.Util;
import com.example.tritracker.R;

public class SpinnerPopupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//getWindow().clearFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
		getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, LayoutParams.FLAG_NOT_TOUCH_MODAL);
		getWindow().setFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
		setContentView(R.layout.misc_spinner);
		Util.wait = this;
	}
    // ...but notify us that it happened.

	  @Override
	  public boolean onTouchEvent(MotionEvent event) {
	    // If we've received a touch notification that the user has touched
	    // outside the app, finish the activity.
	    if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
	      //finish();
	      return false;
	    }
	
	    // Delegate everything else to Activity.
	    return super.onTouchEvent(event);
	  }
}
