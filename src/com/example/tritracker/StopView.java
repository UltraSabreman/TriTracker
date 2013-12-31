package com.example.tritracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class StopView extends Activity {
	private BussArrayAdaptor ar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stop_view);
		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(GlobalData.CurrentStop.Name);

		initList();
		invalidateOptionsMenu();
	}

	void initList() {
		ListView view = (ListView) findViewById(R.id.UIBussList);
		ar = new BussArrayAdaptor(this, GlobalData.CurrentStop.Busses);
		view.setAdapter(ar);
		ar.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stop_view, menu);
		if (Util.favHasStop(GlobalData.CurrentStop))
			menu.findItem(R.id.action_favorite).setIcon(
					R.drawable.ic_action_important);
		else
			menu.findItem(R.id.action_favorite).setIcon(
					R.drawable.ic_action_not_important);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (Util.favHasStop(GlobalData.CurrentStop))
			menu.findItem(R.id.action_favorite).setIcon(
					R.drawable.ic_action_important);
		else
			menu.findItem(R.id.action_favorite).setIcon(
					R.drawable.ic_action_not_important);

		return super.onPrepareOptionsMenu(menu);
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
		case R.id.action_order:
			Util.showToast("Not in yet", Toast.LENGTH_SHORT);
			
			return true;		
		case R.id.action_favorite:
			if (Util.favHasStop(GlobalData.CurrentStop)) {
				GlobalData.Favorites.remove(GlobalData.CurrentStop);
				Util.showToast("Removed stop from favorites.", Toast.LENGTH_SHORT);
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
