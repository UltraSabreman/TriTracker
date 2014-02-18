package com.example.tritracker.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.example.tritracker.R;
import com.example.tritracker.Sorter;
import com.example.tritracker.Stop;
import com.example.tritracker.Timer;
import com.example.tritracker.Util;
import com.example.tritracker.Util.ListType;
import com.example.tritracker.arrayadaptors.RouteDirListArrayAdaptor;
import com.example.tritracker.arrayadaptors.RouteListArrayAdaptor;
import com.example.tritracker.arrayadaptors.RouteStopListArrayAdaptor;
import com.example.tritracker.json.AllRoutesJSONResult.ResultSet.Route;
import com.example.tritracker.json.ForgroundRequestManager;

import java.util.ArrayList;

public class SearchRoutesActivity extends Activity {
	private MainService theService;

	private ArrayList<Route> routes = new ArrayList<Route>();
	private ArrayList<Route> dRoutes = new ArrayList<Route>();
	private TextWatcher rw = null;
	private RouteListArrayAdaptor routeAdaptor;
	private Route curRoute = null;

	private ArrayList<Route.Dir> dirs = new ArrayList<Route.Dir>();
	private ArrayList<Route.Dir> dDirs = new ArrayList<Route.Dir>();
	private TextWatcher dw = null;
	private RouteDirListArrayAdaptor dirAdaptor;

	private ArrayList<Route.Dir.Stop> stops = new ArrayList<Route.Dir.Stop>();
	private ArrayList<Route.Dir.Stop> dStops = new ArrayList<Route.Dir.Stop>();
	private TextWatcher sw = null;
	private RouteStopListArrayAdaptor stopAdaptor;

	public enum DisplayMode {Routes, Dirs, Stops}

	;
	private DisplayMode mode = DisplayMode.Routes;
	private Timer test;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);

		theService = MainService.getService();

		if (!theService.updatingSearchRoutes)
			initRoutes();
		else {
			Util.createSpinner(this);
			test = new Timer(0.5);
			test.addCallBack("check valid", new Timer.onUpdate() {
				@Override
				public void run() {
					if (!theService.updatingSearchRoutes) {
						initRoutes();
						Util.hideSpinner();
						SearchRoutesActivity.this.test.stopTimer();
					}
				}
			});
			test.restartTimer();
		}

	}


	private void initStops(Route.Dir d) {
		if (d == null) return;

		stops.clear();
		for (Route.Dir.Stop s : d.stop)
			stops.add(s);

		if (stops != null) {
			setTitle("Select a Stop");
			clearEverything();

			ListView view = (ListView) findViewById(R.id.RouteList);
			EditText edit = (EditText) findViewById(R.id.UIStopIDBox);
			stopAdaptor = new RouteStopListArrayAdaptor(getApplicationContext(), dStops);
			view.setAdapter(stopAdaptor);
			searchStops(null);
			stopAdaptor.notifyDataSetChanged();

			sw = new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
					EditText edit = (EditText) findViewById(R.id.UIStopIDBox);
					String text = edit.getText().toString();

					searchStops(text);
				}

				@Override
				public void afterTextChanged(Editable editable) {

				}
			};

			edit.addTextChangedListener(sw);
			view.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					Route.Dir.Stop temp = stopAdaptor.getItem(position);
					if (temp != null) {
						getJson(temp.locid);
					}
				}
			});

			mode = DisplayMode.Stops;
		}

	}

	private void initDirs(Route r) {
		if (r == null) return;

		dirs.clear();
		for (Route.Dir d : r.dir)
			dirs.add(d);


		if (dirs != null) {
			setTitle("Select a Direction");
			clearEverything();

			ListView view = (ListView) findViewById(R.id.RouteList);
			EditText edit = (EditText) findViewById(R.id.UIStopIDBox);
			dirAdaptor = new RouteDirListArrayAdaptor(getApplicationContext(), dDirs);
			view.setAdapter(dirAdaptor);
			searchDirs(null);
			dirAdaptor.notifyDataSetChanged();

			dw = new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
					EditText edit = (EditText) findViewById(R.id.UIStopIDBox);
					String text = edit.getText().toString();

					searchDirs(text);
				}

				@Override
				public void afterTextChanged(Editable editable) {

				}
			};
			edit.addTextChangedListener(dw);

			view.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					Route.Dir temp = dirAdaptor.getItem(position);
					if (temp != null)
						initStops(temp);
				}
			});

			mode = DisplayMode.Dirs;
		}
	}

	private void initRoutes() {
		routes = theService.getRoutes();

		if (routes != null) {
			setTitle("Select a Route");
			clearEverything();

			ListView view = (ListView) findViewById(R.id.RouteList);
			EditText edit = (EditText) findViewById(R.id.UIStopIDBox);
			routeAdaptor = new RouteListArrayAdaptor(getApplicationContext(), dRoutes);
			searchRoutes(null);
			new Sorter<Route>(Route.class).sortList(dRoutes, ListType.Routes);
			view.setAdapter(routeAdaptor);
			routeAdaptor.notifyDataSetChanged();

			if (view.getFooterViewsCount() == 0)
				view.addFooterView(getLayoutInflater().inflate(R.layout.misc_seperator, null), null, true);

			rw = new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				}

				@Override
				public void afterTextChanged(Editable editable) {
					searchRoutes(editable.toString());
				}
			};
			edit.addTextChangedListener(rw);

			view.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					Route temp = routeAdaptor.getItem(position);
					if (temp != null) {
						curRoute = temp;
						initDirs(temp);
					}
				}
			});

			mode = DisplayMode.Routes;
		}
	}

	private void clearEverything() {
		ListView view = (ListView) findViewById(R.id.RouteList);
		EditText edit = (EditText) findViewById(R.id.UIStopIDBox);

		edit.removeTextChangedListener(sw);
		edit.removeTextChangedListener(dw);
		edit.removeTextChangedListener(rw);
		view.setOnItemClickListener(null);

		if (stopAdaptor != null) {
			stopAdaptor.notifyDataSetInvalidated();
			stopAdaptor = null;
		}
		if (dirAdaptor != null) {
			dirAdaptor.notifyDataSetInvalidated();
			dirAdaptor = null;
		}
		if (routeAdaptor != null) {
			routeAdaptor.notifyDataSetInvalidated();
			routeAdaptor = null;
		}
	}


	private void getJson(int stop) {
		Util.createSpinner(this);
		ForgroundRequestManager.ResultCallback call = new ForgroundRequestManager.ResultCallback() {
			public void run(Stop s) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Util.hideSpinner();
					}
				});

				Context c = getApplicationContext();

				Intent tempIntent = new Intent(c, StopDetailsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				tempIntent.putExtra("stop", s.StopID);

				c.startActivity(tempIntent);
				theService.doUpdate(false);
			}
		};

		new ForgroundRequestManager(call, this, getApplicationContext(), stop).start();

	}

	void searchStops(String t) {
		dStops.clear();

		if (t == null || t.compareTo("") == 0) dStops.addAll(stops);
		else {
			String searchTerm = t.toLowerCase();

			for (Route.Dir.Stop r : stops) {
				boolean search = false;
				if (r.desc.toLowerCase().contains(searchTerm))
					search = true;
				else if (String.valueOf(r.locid).toLowerCase().contains(searchTerm))
					search = true;

				if (search)
					dStops.add(r);
			}
		}

		stopAdaptor.notifyDataSetChanged();
	}

	void searchDirs(String t) {
		dDirs.clear();

		if (t == null || t.compareTo("") == 0) dDirs.addAll(dirs);
		else {
			String searchTerm = t.toLowerCase();

			for (Route.Dir r : dirs) {
				boolean search = false;
				if (r.desc.toLowerCase().contains(searchTerm))
					search = true;
				else if (r.dir == 1 && "inbound".contains(searchTerm))
					search = true;
				else if (r.dir == 0 && "outbound".contains(searchTerm))
					search = true;

				if (search)
					dDirs.add(r);
			}
		}
		dirAdaptor.notifyDataSetChanged();
	}

	void searchRoutes(String t) {
		dRoutes.clear();

		if (t == null || t.compareTo("") == 0) dRoutes.addAll(routes);
		else {
			String searchTerm = t.toLowerCase();

			for (Route r : routes) {
				boolean search = false;
				if (r.desc.toLowerCase().contains(searchTerm))
					search = true;
				else if (String.valueOf(r.route).toLowerCase().contains(searchTerm))
					search = true;

				if (search)
					dRoutes.add(r);
			}
		}

		routeAdaptor.notifyDataSetChanged();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_actionbar, menu);
		return true;
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
				if (mode == DisplayMode.Routes)
					goBack();
				else if (mode == DisplayMode.Dirs)
					initRoutes();
				else if (mode == DisplayMode.Stops)
					initDirs(curRoute);

				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
