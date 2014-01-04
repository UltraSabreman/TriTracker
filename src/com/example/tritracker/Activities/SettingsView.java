package com.example.tritracker.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.tritracker.GlobalData;
import com.example.tritracker.R;
import com.example.tritracker.Util;

public class SettingsView extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Holo);
		setContentView(R.layout.activity_settings);
		
		Util.parents.push(getClass());
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		EditText np = (EditText) findViewById(R.id.Delay);
		np.setText(String.valueOf(GlobalData.RefreshDelay));
	}

	public void applyChanges(View view) {
		GlobalData.RefreshDelay = Integer.parseInt(((EditText) findViewById(R.id.Delay)).getText().toString());
		goBack();
	}
	
	public void helpRefresh(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle("Auto-refresh delay")
		.setMessage("The delay (in seconds) at witch the app will refresh all stops. The higher the number, the less data-hungry it will be. Set to 0 to dissable");		
		
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	           }
		});
	       
		builder.create().show();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
	private void goBack() {
		Intent parentActivityIntent = new Intent(this, Util.parents.pop());
		parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
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
