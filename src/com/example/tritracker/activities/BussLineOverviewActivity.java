package com.example.tritracker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tritracker.Buss;
import com.example.tritracker.map.Map;
import com.example.tritracker.NotificationHandler;
import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Timer;
import com.example.tritracker.Util;
import com.example.tritracker.arrayadaptors.BussOverviewSpinnerAdaptor;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;

import java.net.ConnectException;

public class BussLineOverviewActivity extends Activity {
	MainService theService = null;
	Stop curStop = null;
	Buss curBuss = null;
	int selection = 0;
	int curArival = 0;
	BussOverviewSpinnerAdaptor adaptor = null;

	Map test = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stopdetails_overview);

		theService = MainService.getService();

		Bundle extra = getIntent().getExtras();
		curStop = theService.getStop(extra.getInt("stopID"));
		selection = extra.getInt("selection");
		curBuss = curStop.Busses.get(selection);

		theService.sub("popup refresh", new Timer.onUpdate() {
			@Override
			public void run() {
				update();
			}
		});

		Spinner spin = (Spinner) findViewById(R.id.spinner);
		final Button alert = (Button) findViewById(R.id.AlertButton);
		Button remind = (Button) findViewById(R.id.ReminderButton);

		MapFragment mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		UiSettings set = mapFrag.getMap().getUiSettings();
			set.setZoomControlsEnabled(false);
			set.setCompassEnabled(false);
			set.setMyLocationButtonEnabled(false);
			set.setAllGesturesEnabled(false);


		test = new Map(mapFrag, getApplicationContext(), this);
		final Timer delay = new Timer(0.1);
		delay.addCallBack("", new Timer.onUpdate(){
			@Override
			public void run() {
				if (!test.isConnected() || theService.updatingMapRoutes) return;
				try {
					delay.stopTimer();
					test.setTrackingLayerEnabled(true);
					test.setRouteLayerEnabled(true);
					test.RouteLayerDraw(String.valueOf(curBuss.Route));
					test.TrackingLayerTransition(curStop, curBuss.Route, curBuss.times.get(curArival).BlockID);

				} catch (ConnectException e) {}
			}
		});
		delay.restartTimer();

		adaptor = new BussOverviewSpinnerAdaptor(this, curStop, curBuss, curBuss.times);
		spin.setAdapter(adaptor);
		adaptor.notifyDataSetChanged();

		if (theService.doesBussHaveAlerts(curBuss))
			alert.setEnabled(true);
		else
			alert.setEnabled(false);


		alert.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent temp = new Intent(BussLineOverviewActivity.this, AlertListActivity.class);
				temp.putExtra("stop", curStop.StopID);
				startActivity(temp);
			}
		});

		remind.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				buildDialouge();
			}
		});

		spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
				curArival = pos;

				update();

				if (test.isConnected())
					test.TrackingLayerSwitchBuss(curBuss.times.get(curArival).BlockID);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				update();
			}
		});
	}

	public void update() {
		curStop = theService.getStop(curStop);
		curBuss = curStop.Busses.get(selection);

		adaptor.notifyDataSetChanged();
		((Spinner) findViewById(R.id.spinner)).requestLayout();

		//test.update(curBuss.BlockID);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		theService.unsub("popup refresh");
	}


	public void buildDialouge() {
		final NotificationHandler rem = theService.getReminder(curBuss.times.get(selection));

		final boolean adding = (rem == null);


		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		final View ourView = getLayoutInflater().inflate(R.layout.stopdetails_reminder, null);
		((TextView) ourView.findViewById(R.id.reminderLabel)).setText(" Min before arrival.");

		final NumberPicker b = (NumberPicker) ourView.findViewById(R.id.reminderTime);

		b.setMaxValue(Math.min(Util.getBussMinutes(adding ? curBuss.times.get(selection) : rem.getBussTime()), 60));
		b.setMinValue(1);

		theService.sub("wheel update", new Timer.onUpdate() {
			@Override
			public void run() {
				b.setMaxValue(Math.min(Util.getBussMinutes(adding ? curBuss.times.get(selection) : rem.getBussTime()), 60));
			}
		});

		builder.setTitle(adding ? "Set Reminder" : "Edit Reminder").setView(ourView);
		if (!adding) {
			if (rem.getTime() > b.getMaxValue())
				b.setValue(b.getMaxValue());
			else
				b.setValue(rem.getTime());
		}

		builder.setPositiveButton(adding ? "Ok" : "Finish", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if (rem != null && rem.IsSet && adding) {
					rem.editNotification(b.getValue());
					Util.showToast("Reminder Updated", Toast.LENGTH_SHORT);
				} else {
					theService.addReminder(new NotificationHandler(getApplicationContext(), getIntent(), curStop, curBuss, curArival, b.getValue()));
					Util.showToast("Reminder Set", Toast.LENGTH_SHORT);
				}
				theService.doUpdate(false);
				theService.unsub("wheel update");
			}
		});

		if (!adding) {
			builder.setNeutralButton("Delete",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							rem.cancelNotification();
							theService.doUpdate(false);
							theService.unsub("wheel update");
						}
					});
		}

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						theService.unsub("wheel update");
					}
				});

		builder.create().show();
	}
}
