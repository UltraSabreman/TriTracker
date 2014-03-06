package com.example.tritracker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.tritracker.R;
import com.example.tritracker.Util;
import com.example.tritracker.json.ForgroundRequestManager;

public class SettingsActivity extends Activity {

	private MainService theService;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);

		theService = MainService.getService();


		EditText np = (EditText) findViewById(R.id.Delay);
		np.setText(String.valueOf(theService.getDelay()));

		np.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
			                              KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					EditText edit = (EditText) findViewById(R.id.Delay);

					theService.setDelay(Integer.parseInt(edit.getText().toString()));
				}
				return false;
			}
		});

		np = (EditText) findViewById(R.id.Radius);
		np.setText(String.valueOf(Math.round(theService.getMapRadius() / 0.3408)));

		np.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
			                              KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
					EditText edit = (EditText) findViewById(R.id.Radius);

					theService.setMapRadius(Double.parseDouble(edit.getText().toString()) * 0.3408);
				}
				return false;
			}
		});

		CheckBox box = (CheckBox) findViewById(R.id.saveMapInfo);

		box.setChecked(theService.getMapSave());
		box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				theService.setMapSave(b);
			}
		});
	}

    public void refresh(View view) {
        Util.messageDiag(
                this,
                new ForgroundRequestManager.checkStops() {
                    public void doStops() {
                        theService.updateSearchRoutes();
                        theService.updateMapRoutes();
                    }
                },
                "Update Route Database?",
                "This will download new route information from TriMet. The file is ~10mb in size, so this process could take a while." +
		        "\n\nDo you wish to continue?"
        );
    }

	public void helpRefresh(View view) {
		Util.messageDiag(
				this,
				null,
				"Auto-refresh delay",
				"The delay (in seconds) at which the app will refresh all necessary information. The higher the number, the less data and battery hungry" +
				" the app will be.");
	}

	public void helpMapSave(View view) {
		Util.messageDiag(
				this,
				null,
				"Save Map Settings",
				"This will allow the app to save your map configuration (aka: which routes and buses you're displaying). When drawing many routes, this will result in a slower" +
				" initial map load.");
	}

	public void helpRadius(View view) {
		Util.messageDiag(
				this,
				null,
				"Map Search Radius",
				"When searching for nearby stops, the map will use this value to determine how far to look.\\nn2048 feet is equal to a half a mile.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.settings_action_bar, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		theService.doUpdate(false);
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
