package com.example.tritracker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
import com.example.tritracker.activities.MainService.LocalBinder;
import com.example.tritracker.arrayadaptors.BussListArrayAdaptor;

public class StopDetailsActivity extends Activity {
	private Buss menuBuss = null;

	private Stop curStop;
	private BussListArrayAdaptor adaptor;
	

	private MainService theService;
	private boolean bound;
	
	@Override 
	public void onStart() {
		super.onStart();
		Intent intent = new Intent(this, MainService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
    @Override
    protected void onStop() {
        super.onStop();
        theService.unsub("BussList");
        if (bound) 
            unbindService(mConnection);
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            theService = binder.getService();
            
            curStop = theService.getStop(curStop);
            
            theService.sub("BussList", new Timer.onUpdate() {
            	public void run() {
            		StopDetailsActivity.this.onUpdate();
            	}
            });
            
            bound = true;
        }

        
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
    
    public void onUpdate() {
    	curStop = theService.getStop(curStop);
    	
		runOnUiThread(new Runnable() {
			@Override
			public void run() {					
				if (adaptor != null) {
					adaptor.clear();
					adaptor.addAll(curStop.Busses);
					adaptor.updateStop(curStop);
					adaptor.notifyDataSetChanged();
				}
			}
		});
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stop_details_layout);
		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle extras = getIntent().getExtras();
		curStop = extras.getParcelable("stop");
		
		setTitle("Stop ID: " + curStop.StopID);

		TextView StopName = (TextView) findViewById(R.id.UIStopInfoName);
		TextView StopDir = (TextView) findViewById(R.id.UIStopInfoDirection);

		StopName.setText(curStop.Name);
		StopName.setSelected(true);
		StopDir.setText(curStop.Direction);

		final Activity act = this;
		if (curStop.Alerts != null
				&& curStop.Alerts.size() != 0) {
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
		invalidateOptionsMenu();
	}

	@Override
	public void onRestart() {
		super.onRestart();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	void initList() {
		if (curStop.Busses == null || curStop.Busses.size() == 0) {
			TextView arrival = (TextView) findViewById(R.id.NoArrivals);
			arrival.setVisibility(View.VISIBLE);
		} else {
			final Activity act = this;
			final ListView view = (ListView) findViewById(R.id.UIBussList);
			RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainDetailsView);
			adaptor = new BussListArrayAdaptor(this, curStop);
			
			view.setAdapter(adaptor);
			adaptor.notifyDataSetChanged();
			registerForContextMenu(layout);
			registerForContextMenu(view);
			
			OnLongClickListener longc = new OnLongClickListener() {
				public boolean onLongClick(View view) {
					act.openContextMenu(view);
					return false;
				}
			};
			
			view.setOnLongClickListener(longc);
			//layout.setOnLongClickListener(longc);
			
			layout.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					MenuInflater inflater = getMenuInflater();
					inflater.inflate(R.menu.stop_list_context_menu, menu);
					
					((MenuItem) menu.findItem(R.id.action_clear_list)).setVisible(false);
				}
			});
			
			view.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					MenuInflater inflater = getMenuInflater();
					if (curStop.Alerts != null && curStop.Alerts.size() > 0)
						inflater.inflate(R.menu.buss_alert_context_menu, menu);
					else
						inflater.inflate(R.menu.buss_context_menu, menu);

					if (menuBuss != null && menuBuss.notification != null && menuBuss.notification.IsSet) {
						((MenuItem) menu.findItem(R.id.action_create_reminder)).setVisible(false);
						((MenuItem) menu.findItem(R.id.action_edit_reminder)).setVisible(true);
						((MenuItem) menu.findItem(R.id.action_cancel_reminder)).setVisible(true);
					}
				}
			});
			view.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,	int pos, long id) {
					menuBuss = curStop.Busses.get(pos);
					if (menuBuss != null)
						act.openContextMenu(view);	
				}
			});

		}

	}

	public void buildDialouge(final boolean add) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		final View ourView = getLayoutInflater().inflate(R.layout.set_reminder,	null);
		final TextView t = (TextView) ourView.findViewById(R.id.reminderLabel);
		
		t.setText("0 min before buss.");
		
		final SeekBar b = (SeekBar) ourView.findViewById(R.id.reminderTime);
		b.setMax(Math.min(Util.getBussMinutes(menuBuss), 60));

		b.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				t.setText(progress + " min before buss arrives.");
			}
		});

		if (add)
			builder.setTitle("Set a Reminder For:").setView(ourView);
		else {
			Buss buss = curStop.getBuss(menuBuss);
			builder.setTitle("Set a Reminder For:")
					.setMessage(
							"Previous reminder set at: "
									+ buss.notification.getTime())
					.setView(ourView);
			if (buss.notification.getTime() > b.getMax())
				b.setProgress(b.getMax());
			else
				b.setProgress(buss.notification.getTime());
		}

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Buss buss = curStop.getBuss(menuBuss);
				if (buss.notification != null && buss.notification.IsSet && add) {
					buss.notification.editNotification(b.getProgress());
				} else
					buss.setNotification(new NotificationHandler(
							getApplicationContext(), getIntent(),
							curStop, menuBuss, b.getProgress()));
				adaptor.notifyDataSetChanged();
			}
		});

		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});

		builder.create().show();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_create_reminder:
				buildDialouge(true);
				Util.showToast("Reminder Set", Toast.LENGTH_SHORT);
				return true;
			case R.id.action_edit_reminder:
				buildDialouge(false);
				Util.showToast("Reminder Updated", Toast.LENGTH_SHORT);
				return true;
			case R.id.action_cancel_reminder:
				Buss buss = curStop.getBuss(menuBuss);
				if (buss.notification != null && buss.notification.IsSet)
					buss.notification.cancelNotification();
				Util.showToast("Reminder Canceled", Toast.LENGTH_SHORT);
				return true;
			case R.id.action_sort_list:
				Stop stop = theService.getStop(curStop);
				new Sorter<Buss>(Buss.class, theService).sortUI(this, ListType.Busses, stop.Busses,
						new Timer.onUpdate() {
							public void run() {
								adaptor.notifyDataSetChanged();
								theService.doUpdate(false);
							}
						});		
				return true;
			case R.id.action_view_alerts:
				Intent temp = new Intent(this, AlertListActivity.class);
				temp.putExtra("stop", curStop);
				startActivity(temp);
				return true;
			default:
				
		}
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
