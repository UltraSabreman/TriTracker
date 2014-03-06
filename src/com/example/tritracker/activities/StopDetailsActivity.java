package com.example.tritracker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tritracker.Alert;
import com.example.tritracker.Buss;
import com.example.tritracker.NotificationHandler;
import com.example.tritracker.R;
import com.example.tritracker.Sorter;
import com.example.tritracker.Stop;
import com.example.tritracker.Timer;
import com.example.tritracker.Util;
import com.example.tritracker.Util.ListType;
import com.example.tritracker.arrayadaptors.BussListArrayAdaptor;

import java.util.ArrayList;

public class StopDetailsActivity extends Activity {
	private Buss menuBuss = null;

	private Stop curStop;
	private BussListArrayAdaptor adaptor;
    private ArrayList<Buss> test = new ArrayList<Buss>();

	private MainService theService;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stopdetails);
		Util.parents.push(getClass());

		theService = MainService.getService();
        while(theService == null) {
            try {
                Thread.sleep((long) 0.01);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            theService = MainService.getService();
        }

		getActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle extras = getIntent().getExtras();
		int id = extras.getInt("stop");
		curStop = theService.getStop(id);

		setTitle("Stop ID: " + id);

		TextView StopName = (TextView) findViewById(R.id.UIStopInfoName);
		TextView StopDir = (TextView) findViewById(R.id.UIStopInfoDirection);

		StopName.setText(curStop.Name);
		StopName.setSelected(true);
		StopDir.setText(curStop.Direction);


		final Activity act = this;
		ArrayList<Alert> a = theService.getStopAlerts(curStop);
		if (a != null && a.size() != 0) {
			View alert = (View) findViewById(R.id.alertBackground);
			alert.setVisibility(View.VISIBLE);

			alert.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent temp = new Intent(act, AlertListActivity.class);
					temp.putExtra("stop", curStop.StopID);
					startActivity(temp);
				}
			});

		} else
			((View) findViewById(R.id.alertBackground)).setVisibility(View.INVISIBLE);

		initList();

		theService.sub("BussList", new Timer.onUpdate() {
			public void run() {
				onUpdate();
			}
		});
	}


	public void onUpdate() {
		curStop = theService.getStop(curStop);
        test = curStop.Busses;
		this.runOnUiThread(new Runnable() {
			public void run() {
				if (adaptor != null) {
					adaptor.updateStop(curStop);
					adaptor.notifyDataSetChanged();
				}
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
		theService.unsub("BussList");
	}

	@Override
	protected void onResume() {
		super.onResume();
		theService.sub("BussList", new Timer.onUpdate() {
			public void run() {
				onUpdate();
			}
		});
		theService.doUpdate(true);
	}

	void initList() {
        test = curStop.Busses;
		if (test == null || test.size() == 0) {
			TextView arrival = (TextView) findViewById(R.id.NoArrivals);
			arrival.setVisibility(View.VISIBLE);
		} else {
			final Activity act = this;
			final ListView listView = (ListView) findViewById(R.id.UIBussList);
			final RelativeLayout layout = (RelativeLayout) findViewById(R.id.longClickCatcher);
			adaptor = new BussListArrayAdaptor(this, test);
			adaptor.updateStop(curStop);

			listView.setAdapter(adaptor);
			adaptor.notifyDataSetChanged();
			registerForContextMenu(layout);

			if (listView.getFooterViewsCount() == 0)
				listView.addFooterView(getLayoutInflater().inflate(R.layout.misc_seperator, null), null, true);

			layout.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

					ArrayList<Alert> a = theService.getStopAlerts(curStop);
					if (a != null && a.size() > 0) {
						menu.add(0, R.id.menu_action_view_alerts, ContextMenu.NONE, "View Relevant Alerts");
					}

					if (menuBuss != null) {
						NotificationHandler n = theService.getReminder(menuBuss);
						if (n != null && n.IsSet) {
							menu.add(0, R.id.menu_action_edit_reminder, ContextMenu.NONE, "Edit Reminder");
							menu.add(0, R.id.menu_action_remove_reminder, ContextMenu.NONE, "Remove Reminder");
						} else
							menu.add(0, R.id.menu_action_add_reminder, ContextMenu.NONE, "Add Reminder");
					}

					menu.add(0, R.id.menu_action_sort_list, ContextMenu.NONE, "Sort Arrivals");
				}
			});


			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
					Intent temp = new Intent(getApplicationContext(), BussLineOverviewActivity.class);
					temp.putExtra("stopID", curStop.StopID);
					temp.putExtra("selection", pos);
					startActivity(temp);

					listView.clearChoices();
					listView.requestLayout();
				}
			});

			listView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					act.openContextMenu(v);
					return false;
				}
			});

		}

	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		menuBuss = null;
		super.onContextMenuClosed(menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_action_add_reminder:
				return true;
			case R.id.menu_action_edit_reminder:
				//buildDialouge(menuBuss, false);
				return true;
			case R.id.menu_action_remove_reminder:
				Buss buss = curStop.getBuss(menuBuss);
				NotificationHandler n = theService.getReminder(buss);
				if (n != null && n.IsSet)
					n.cancelNotification();
				Util.showToast("Reminder Canceled", Toast.LENGTH_SHORT);
				adaptor.notifyDataSetChanged();
				return true;
			case R.id.menu_action_sort_list:
				Stop stop = theService.getStop(curStop);
				new Sorter<Buss>(Buss.class).sortUI(this, ListType.Busses, stop.Busses,
						new Timer.onUpdate() {
							public void run() {
								adaptor.notifyDataSetChanged();
								theService.doUpdate(false);
							}
						});
				return true;
			case R.id.menu_action_view_alerts:

				return true;

		}
		menuBuss = null;
		theService.doUpdate(false);
		adaptor.notifyDataSetChanged();
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stopdetails_actionbar, menu);
		menu = refreshFavIcon(menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu = refreshFavIcon(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	public Menu refreshFavIcon(Menu menu) {
		if (curStop.inFavorites)
			menu.findItem(R.id.action_favorite).setIcon(
					R.drawable.ic_action_important);
		else
			menu.findItem(R.id.action_favorite).setIcon(
					R.drawable.ic_action_not_important);
		return menu;
	}

	private void goBack() {
		Intent parentActivityIntent = new Intent(this, Util.parents.pop());
		parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		NavUtils.navigateUpTo(this, parentActivityIntent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				goBack();
				return true;
			case R.id.action_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			case R.id.action_favorite:
				if (curStop.inFavorites) {
					curStop.inFavorites = false;

					Util.showToast("Removed stop from favorites.", Toast.LENGTH_SHORT);
				} else {
					curStop.inFavorites = true;
					Util.showToast("Added stop to favorites.", Toast.LENGTH_SHORT);
				}

				invalidateOptionsMenu();
				theService.doUpdate(false);
				return true;
			case R.id.action_map:
				Intent temp = new Intent(getApplicationContext(), MapActivity.class);
				temp.putExtra("lat", curStop.Latitude);
				temp.putExtra("lng", curStop.Longitude);
				temp.putExtra("stopid", curStop.StopID);
				startActivity(temp);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
