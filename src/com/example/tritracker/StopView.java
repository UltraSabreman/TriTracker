package com.example.tritracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class StopView extends Activity {
	private Buss menuBuss = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stop_detail);
		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle("Stop ID: " + GlobalData.CurrentStop.StopID);

		TextView StopName = (TextView) findViewById(R.id.UIStopInfoName);
		TextView StopDir = (TextView) findViewById(R.id.UIStopInfoDirection);
		
		StopName.setText(GlobalData.CurrentStop.Name);
		StopName.setSelected(true);
		StopDir.setText(GlobalData.CurrentStop.Direction);
				
		initList();		
		invalidateOptionsMenu();
	}
	
	@Override
	public void onRestart() {
		GlobalData.Orientation = getResources().getConfiguration().orientation;
		GlobalData.bussAdaptor.notifyDataSetChanged();
		super.onRestart();
	}
	
	@Override
	public void onDestroy() {
		GlobalData.Orientation = getResources().getConfiguration().orientation;
		//GlobalData.bussAdaptor.notifyDataSetChanged();
		super.onDestroy();
	}
	

	void initList() {
		if (GlobalData.CurrentStop.Busses == null || GlobalData.CurrentStop.Busses.size() == 0) {
			TextView arrival = (TextView) findViewById(R.id.NoArrivals);
			arrival.setVisibility(View.VISIBLE);
			if (GlobalData.bussAdaptor != null) {
				GlobalData.bussAdaptor.notifyDataSetInvalidated();
				GlobalData.bussAdaptor.clear();
			}			
		}else {
			final ListView view = (ListView) findViewById(R.id.UIBussList);		
			GlobalData.bussAdaptor = new BussArrayAdaptor(this, GlobalData.CurrentStop.Busses);
			view.setAdapter(GlobalData.bussAdaptor);
			GlobalData.bussAdaptor.notifyDataSetChanged();
			registerForContextMenu(view);			
			
			view.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				    MenuInflater inflater = getMenuInflater();
				    inflater.inflate(R.menu.stop_view_context, menu);

				   
			        if (menuBuss != null && menuBuss.notification != null && menuBuss.notification.IsSet) {
			        	((MenuItem) menu.findItem(R.id.action_create_reminder)).setVisible(false);
			        	((MenuItem) menu.findItem(R.id.action_edit_reminder)).setVisible(true);
			        	((MenuItem) menu.findItem(R.id.action_cancel_reminder)).setVisible(true);
			        }
				}
			});

			final Activity act = this;
			view.setOnItemLongClickListener(new OnItemLongClickListener() {
				 public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
					menuBuss = GlobalData.CurrentStop.Busses.get(pos);
					if (menuBuss != null) {
						act.openContextMenu(view);
						return true;
					}
					
					return false;
				}
			});
		}

	}
	
	public void buildDialouge(final boolean add) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		final View ourView = getLayoutInflater().inflate(R.layout.set_reminder, null);
		final TextView t = (TextView) ourView.findViewById(R.id.reminderLabel);
		t.setText("0 min before buss.");
		final SeekBar b = (SeekBar) ourView.findViewById(R.id.reminderTime);
		b.setMax(Util.getBussMinutes(menuBuss));	
		//if (!add)
			//b.setProgress();
		
		b.setOnSeekBarChangeListener(new  SeekBar.OnSeekBarChangeListener() {
			public  void  onStopTrackingTouch(SeekBar seekBar) {
            } 
 
            public  void  onStartTrackingTouch(SeekBar seekBar) { 
            } 
			@Override       
			public  void  onProgressChanged(SeekBar seekBar, int  progress, boolean  fromUser) {     
			    t.setText(progress + " min before buss.");
			}       
		});
		
		builder.setTitle("Set a Reminder For:")
				.setView(ourView);
		
		
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   Buss buss = GlobalData.CurrentStop.getBuss(menuBuss);
	        	   if (buss.notification != null && buss.notification.IsSet && add) {
	        		   buss.notification.editNotification(b.getProgress());
	        	   } else
	        		   buss.setNotification(new NotificationHandler(getApplicationContext(), getIntent(), GlobalData.CurrentStop, menuBuss, b.getProgress()));
	           }
	       });
		
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
	        	Buss buss = GlobalData.CurrentStop.getBuss(menuBuss);
	        	if (buss.notification != null && buss.notification.IsSet) 
	        		buss.notification.cancelNotification();
	        	Util.showToast("Reminder Canceled", Toast.LENGTH_SHORT);
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stop_view, menu);
		menu = refreshFavIcon(menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu = refreshFavIcon(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	public Menu refreshFavIcon(Menu menu) {
		if (Util.favHasStop(GlobalData.CurrentStop))
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
			Util.showToast("Not in yet", Toast.LENGTH_SHORT);
			return true;
		case R.id.action_sort:
			GlobalData.StopOrder = (GlobalData.StopOrder + 1) % 3;
			if (Util.sortList(2)) {
				Util.showToast("Busses Sorted By: " + (GlobalData.StopOrder == 0 ? "Name" : (GlobalData.StopOrder == 1 ? "Route" : "Time")), Toast.LENGTH_SHORT);
				GlobalData.bussAdaptor.notifyDataSetChanged();
				
			} else {
				Util.showToast("Nothing to sort", Toast.LENGTH_SHORT);
				GlobalData.StopOrder = (GlobalData.StopOrder - 1) % 3;
			}
			return true;
		case R.id.action_favorite:
			if (Util.favHasStop(GlobalData.CurrentStop)) {
				GlobalData.Favorites.remove(GlobalData.CurrentStop);
				Util.removeStop(GlobalData.CurrentStop, GlobalData.Favorites);
				Util.showToast("Removed stop from favorites.",
						Toast.LENGTH_SHORT);
			} else {
				GlobalData.Favorites.add(GlobalData.CurrentStop);
				Util.showToast("Added stop to favorites.", Toast.LENGTH_SHORT);
			}

			invalidateOptionsMenu();

			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
