package com.example.tritracker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tritracker.Buss;
import com.example.tritracker.NotificationHandler;
import com.example.tritracker.R;
import com.example.tritracker.Sorter;
import com.example.tritracker.Stop;
import com.example.tritracker.Timer;
import com.example.tritracker.Util;
import com.example.tritracker.Util.ListType;
import com.example.tritracker.arrayadaptors.BussListArrayAdaptor;

public class StopDetailsActivity extends Activity {
	private Buss menuBuss = null;

	private Stop curStop;
	private BussListArrayAdaptor adaptor;

	private MainService theService;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stop_details_layout);
		Util.parents.push(getClass());

		theService = MainService.getService();
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle extras = getIntent().getExtras();
		int id = extras.getInt("stop");
		curStop = theService.getStop(id);
		
		setTitle("Stop ID: " + curStop.StopID);

		TextView StopName = (TextView) findViewById(R.id.UIStopInfoName);
		TextView StopDir = (TextView) findViewById(R.id.UIStopInfoDirection);

		StopName.setText(curStop.Name);
		StopName.setSelected(true);
		StopDir.setText(curStop.Direction);
	

		final Activity act = this;
		if (curStop.Alerts != null && curStop.Alerts.size() != 0) {
			View alert = (View) findViewById(R.id.alertBackground);
			alert.setVisibility(View.VISIBLE);

			alert.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent temp = new Intent(act, AlertListActivity.class);
					temp.putExtra("stop", curStop);
					startActivity(temp);
				}
			});

		} else
			((View) findViewById(R.id.alertBackground)).setVisibility(View.INVISIBLE);	
		
		initList();
	}

  
    public void onUpdate() {
    	curStop = theService.getStop(curStop);
    	
		runOnUiThread(new Runnable() {
			@Override
			public void run() {					
				if (adaptor != null) {
					Stop s = StopDetailsActivity.this.curStop;
					adaptor.updateStop(s);
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
    protected void onStart() {
    	super.onStart();
		theService.sub("BussList", new Timer.onUpdate() {
			public void run() {
				onUpdate();
			}
		});
    }

	void initList() {
		if (curStop.Busses == null || curStop.Busses.size() == 0) {
			TextView arrival = (TextView) findViewById(R.id.NoArrivals);
			arrival.setVisibility(View.VISIBLE);
		} else {
			final Activity act = this;
			final ListView listView = (ListView) findViewById(R.id.UIBussList);
			final RelativeLayout layout = (RelativeLayout) findViewById(R.id.longClickCatcher);
			adaptor = new BussListArrayAdaptor(this, curStop.Busses);
			adaptor.updateStop(curStop);
			
			listView.setAdapter(adaptor);
			adaptor.notifyDataSetChanged();
			registerForContextMenu(layout);

			if (listView.getFooterViewsCount() == 0)
				listView.addFooterView(getLayoutInflater().inflate(R.layout.seperator, null), null, true);
			
			layout.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					
					if (curStop.Alerts != null && curStop.Alerts.size() > 0) {
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
				public void onItemClick(AdapterView<?> arg0, View arg1,	int pos, long id) {
					menuBuss = curStop.Busses.get(pos);
					act.openContextMenu(arg1);
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

	public void buildDialouge(final Buss theBuss, final boolean add) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		final View ourView = getLayoutInflater().inflate(R.layout.set_reminder,	null);
		((TextView) ourView.findViewById(R.id.reminderLabel)).setText(" Min before arrival.");

		final NumberPicker b = (NumberPicker) ourView.findViewById(R.id.reminderTime);

		b.setMaxValue(Math.min(Util.getBussMinutes(theBuss), 60));
		b.setMinValue(1);
		
		theService.sub("wheel update", new Timer.onUpdate() {
			@Override
			public void run() {
				b.setMaxValue(Math.min(Util.getBussMinutes(theBuss), 60));
			}
		});

		builder.setTitle("Set a Reminder").setView(ourView);
		if (!add) {
			Buss buss = curStop.getBuss(theBuss);
			NotificationHandler n = theService.getReminder(buss);
			if (n.getTime() > b.getMaxValue())
				b.setValue(b.getMaxValue());
			else
				b.setValue(n.getTime());
		}

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Buss buss = curStop.getBuss(theBuss);
				NotificationHandler n = theService.getReminder(buss);
				if (n != null && n.IsSet && add) {
					n.editNotification(b.getValue());
					Util.showToast("Reminder Updated", Toast.LENGTH_SHORT);
				} else {
					theService.addReminder(new NotificationHandler(getApplicationContext(), getIntent(), curStop, theBuss, b.getValue()));
					Util.showToast("Reminder Set", Toast.LENGTH_SHORT);
				}
				adaptor.notifyDataSetChanged();
				theService.unsub("wheel update");
			}
		});

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						theService.unsub("wheel update");
					}
				});

		builder.create().show();
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
				buildDialouge(menuBuss, true);
				return true;
			case R.id.menu_action_edit_reminder:
				buildDialouge(menuBuss, false);
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
				Intent temp = new Intent(this, AlertListActivity.class);
				temp.putExtra("stop", curStop);
				startActivity(temp);
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
		getMenuInflater().inflate(R.menu.stop_details_action_bar, menu);
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


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent parentActivityIntent = new Intent(this, Util.parents.pop());
			parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			NavUtils.navigateUpTo(this, parentActivityIntent);

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
			startActivity(temp);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
