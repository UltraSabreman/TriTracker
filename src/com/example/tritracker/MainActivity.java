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

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.tritracker.MESSAGE";
	
	private ArrayList<Stop> favorites = new ArrayList<Stop>();
	private ArrayList<Stop> history = new ArrayList<Stop>();
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
        
        favAdaptor=new StopArrayAdaptor(this, R.layout.stoplayout, favorites);
        histAdaptor=new StopArrayAdaptor(this, R.layout.stoplayout, history);
        
        view.setAdapter(favAdaptor);
        favorites.add(new Stop(temp));
        favorites.add(new Stop(temp2));
        favorites.add(new Stop(new StopData()));
        //view.setAdapter(adapter);
        favAdaptor.notifyDataSetChanged();
        
        
        /*for (Stop s : test){
        	//System.out.println("world");
        	//TextView t = new TextView(this);
        	//t.setText();
        	strings.add(s.GetStopLocation() + "\t" + s.GetStopID());
        	adapter.notifyDataSetChanged();
        	//ArrayAdapter<String> myarrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myList);
        	//view.setAdapter(myarrayAdapter);
        	//view.setTextFilterEnabled(true);
        	//view.addView(t);
        }*/
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
