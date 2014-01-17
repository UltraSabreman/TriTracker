package com.example.tritracker.Activities;

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
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tritracker.Buss;
import com.example.tritracker.GlobalData;
import com.example.tritracker.NotificationHandler;
import com.example.tritracker.R;
import com.example.tritracker.Util;
import com.example.tritracker.ArrayAdaptors.BussArrayAdaptor;
import com.example.tritracker.Stop.Alert;

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

		final Activity act = this;
		if (GlobalData.CurrentStop.Alerts != null
				&& GlobalData.CurrentStop.Alerts.size() != 0) {
			View alert = (View) findViewById(R.id.alertBackground);
			alert.setVisibility(View.VISIBLE);

			alert.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(act, AlertListView.class));
				}
			});

		} else
			((View) findViewById(R.id.alertBackground))
					.setVisibility(View.INVISIBLE);

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
		System.out.println("---lolwut---1---");
		GlobalData.Orientation = getResources().getConfiguration().orientation;
		System.out.println("---lolwut---2---");
		if (GlobalData.bussAdaptor != null)
			GlobalData.bussAdaptor.notifyDataSetChanged();
		System.out.println("---lolwut---3---");
		super.onDestroy();
		System.out.println("---lolwut---4---");
	}

	void initList() {
		if (GlobalData.CurrentStop.Busses == null
				|| GlobalData.CurrentStop.Busses.size() == 0) {
			TextView arrival = (TextView) findViewById(R.id.NoArrivals);
			arrival.setVisibility(View.VISIBLE);
			if (GlobalData.bussAdaptor != null) {
				GlobalData.bussAdaptor.notifyDataSetInvalidated();
				GlobalData.bussAdaptor.clear();
			}
		} else {
			final ListView view = (ListView) findViewById(R.id.UIBussList);
			GlobalData.bussAdaptor = new BussArrayAdaptor(this,
					GlobalData.CurrentStop.Busses);
			view.setAdapter(GlobalData.bussAdaptor);
			GlobalData.bussAdaptor.notifyDataSetChanged();
			registerForContextMenu(view);

			view.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v,
						ContextMenuInfo menuInfo) {
					MenuInflater inflater = getMenuInflater();
					inflater.inflate(R.menu.stop_view_context, menu);

					if (menuBuss != null && menuBuss.notification != null
							&& menuBuss.notification.IsSet) {
						((MenuItem) menu.findItem(R.id.action_create_reminder))
								.setVisible(false);
						((MenuItem) menu.findItem(R.id.action_edit_reminder))
								.setVisible(true);
						((MenuItem) menu.findItem(R.id.action_cancel_reminder))
								.setVisible(true);
					}
				}
			});

			final Activity act = this;
			view.setOnItemLongClickListener(new OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int pos, long id) {
					menuBuss = GlobalData.CurrentStop.Busses.get(pos);
					if (menuBuss != null) {
						act.openContextMenu(view);
						return true;
					}

					return false;
				}
			});

			view.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View view,
						int pos, long id) {
					Buss buss = GlobalData.CurrentStop.Busses.get(pos);
					boolean affected = false;
					if (GlobalData.CurrentStop.Alerts != null) {
						for (Alert d : GlobalData.CurrentStop.Alerts) {
							if (d.AffectedLine == buss.Route) {
								affected = true;
								break;
							}
						}
						if (affected)
							view.getContext().startActivity(
									new Intent(view.getContext(),
											AlertListView.class));
					}
				}
			});
		}

	}

	public void buildDialouge(final boolean add) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		final View ourView = getLayoutInflater().inflate(R.layout.set_reminder,
				null);
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
			Buss buss = GlobalData.CurrentStop.getBuss(menuBuss);
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
				Buss buss = GlobalData.CurrentStop.getBuss(menuBuss);
				if (buss.notification != null && buss.notification.IsSet && add) {
					buss.notification.editNotification(b.getProgress());
				} else
					buss.setNotification(new NotificationHandler(
							getApplicationContext(), getIntent(),
							GlobalData.CurrentStop, menuBuss, b.getProgress()));
				GlobalData.bussAdaptor.notifyDataSetChanged();
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
			Util.refreshAdaptors();
			Util.showToast("Reminder Set", Toast.LENGTH_SHORT);
			return true;
		case R.id.action_edit_reminder:
			buildDialouge(false);
			Util.refreshAdaptors();
			Util.showToast("Reminder Updated", Toast.LENGTH_SHORT);
			return true;
		case R.id.action_cancel_reminder:
			Buss buss = GlobalData.CurrentStop.getBuss(menuBuss);
			if (buss.notification != null && buss.notification.IsSet)
				buss.notification.cancelNotification();
			Util.refreshAdaptors();
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
			startActivity(new Intent(this, SettingsView.class));
			return true;
		case R.id.action_sort:
			Util.buildSortDialog((Activity) this, 2);
			GlobalData.bussAdaptor.notifyDataSetChanged();
			return true;
		case R.id.action_favorite:
			if (Util.favHasStop(GlobalData.CurrentStop)) {
				GlobalData.favAdaptor.remove(GlobalData.CurrentStop);
				// Util.removeStop(GlobalData.CurrentStop,
				// GlobalData.Favorites);
				Util.showToast("Removed stop from favorites.",
						Toast.LENGTH_SHORT);
			} else {
				GlobalData.favAdaptor.add(GlobalData.CurrentStop);
				Util.showToast("Added stop to favorites.", Toast.LENGTH_SHORT);
			}

			invalidateOptionsMenu();

			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
