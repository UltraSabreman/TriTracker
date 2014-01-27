package com.example.tritracker.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.tritracker.R;
import com.example.tritracker.Sorter;
import com.example.tritracker.Stop;
import com.example.tritracker.Timer;
import com.example.tritracker.Util;
import com.example.tritracker.Util.ListType;
import com.example.tritracker.arrayadaptors.RouteListArrayAdaptor;
import com.example.tritracker.json.AllRoutesJSONResult.ResultSet.Route;

public class SearchRoutesActivity extends Activity {
	private MainService theService;
	private ArrayList<Route> routes = new ArrayList<Route>();
	private RouteListArrayAdaptor ar;
	private Timer test;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_routes);
		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		theService = MainService.getService();
		
		if (!theService.isUpdating)
			init();
		else {
			Util.createSpinner(this);
			test = new Timer(0.5);
			test.addCallBack("check valid", new Timer.onUpdate() {
				@Override
				public void run() {
					if (!theService.isUpdating) {			
						init();
						Util.hideSpinner();
						test.stopTimer();
					}
				}
			});
			test.restartTimer();
		}
	}

	private void init() {
		routes = theService.getRoutes();
		
		if (routes != null) {			
			ListView view = (ListView) findViewById(R.id.RouteList);
			EditText edit = (EditText) findViewById(R.id.UIStopIDBox);
			ar = new RouteListArrayAdaptor(getApplicationContext(), routes);
			new Sorter<Route>(Route.class).sortList(routes, ListType.Routes);
			view.setAdapter(ar);
			ar.notifyDataSetChanged();
			
		
			if (view.getFooterViewsCount() == 0)
				view.addFooterView(getLayoutInflater().inflate(R.layout.seperator, null), null, true);

			edit.setOnEditorActionListener(new OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (event != null) {
						EditText edit = (EditText) findViewById(R.id.UIStopIDBox);
						String text = edit.getText().toString();
						
						//TODO: filter stuff here.
					}
					return false;
				}
			});	
			
			view.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,	int position, long arg3) {
					Route temp = ar.getItem(position);
					//if (temp != null) 
						//TODO: do stuff here.
				}
			});
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_routes, menu);
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
			goBack();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
