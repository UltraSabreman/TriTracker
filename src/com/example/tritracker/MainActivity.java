package com.example.tritracker;


import java.util.ArrayList;
import java.util.Vector;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity{
	public final static String EXTRA_MESSAGE = "com.example.tritracker.MESSAGE";
	
	private ArrayList<Stop> favorites = new ArrayList<Stop>();
	private ArrayList<Stop> history = new ArrayList<Stop>();
	private Stop tempSavedStop;
	private int index;
	StopArrayAdaptor favAdaptor, histAdaptor;
	//sdfds

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        StopData temp = new StopData();
        temp.Location = "My Street Over Here";
        temp.StopID = 1337;
        temp.BussLines = new Vector<Buss>();
        temp.BussLines.add(new Buss());
        
        StopData temp2 = new StopData();
        temp2.Location = "Mclaughlin and PArk";
        temp2.StopID = 3848;
        temp2.BussLines = new Vector<Buss>();
        temp2.BussLines.add(new Buss());
        
        //test.add();
        ListView view = (ListView) findViewById(R.id.UIStopList);
        EditText edit = (EditText) findViewById(R.id.UIStopIDBox);
        
        edit.setOnEditorActionListener(new OnEditorActionListener(){    
        	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
            	EditText edit = (EditText) findViewById(R.id.UIStopIDBox);
            	System.out.println(edit.getText().toString());
            }    
            return false;
        }});
        
        //view.setOnIt
        
        favAdaptor=new StopArrayAdaptor(this, R.layout.stoplayout, favorites);
        histAdaptor=new StopArrayAdaptor(this, R.layout.stoplayout, history);
        
        view.setAdapter(favAdaptor);
        for (int i = 0; i < 20; i++)
        	favorites.add(new Stop(temp));

        favAdaptor.notifyDataSetChanged();
        
        testList();
    }

    public void testList() {
        ListView listView = (ListView) findViewById(R.id.UIStopList);
        
        // Create a ListView-specific touch listener. ListViews are given special treatment because
        // by default they handle touches for their list items... i.e. they're in charge of drawing
        // the pressed state (the list selector), handling list item clicks, etc.
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(listView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                	tempSavedStop = favorites.get(position);
                                	index = position;
                                	favAdaptor.remove(favAdaptor.getItem(position));
                                	Toast.makeText(getApplicationContext(), "lol", android.R.integer.config_shortAnimTime).show();
                                }
                                favAdaptor.notifyDataSetChanged();
                            }
                        });
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener(touchListener.makeScrollListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_action_bar, menu);
	    return super.onCreateOptionsMenu(menu);
    }    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                //openSearch();
            	favorites.add(new Stop(new StopData()));
            	favAdaptor.notifyDataSetChanged();
            	System.out.println("Search");
                return true;
            case R.id.action_settings:            	 
                //openSettings();
            	System.out.println("Settings");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
}
