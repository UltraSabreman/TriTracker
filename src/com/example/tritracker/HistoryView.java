package com.example.tritracker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.example.tritracker.json.ResultSet;
import com.example.tritracker.json.results;
import com.google.gson.Gson;

public class HistoryView extends Activity {
	public ArrayList<Stop> history = new ArrayList<Stop>();
	HistStopArrayAdaptor histAdaptor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_view);
		Util.parents.push(getClass());
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		subscribeToEdit();
		initList();
	}
	
	
    private void subscribeToEdit(){
        EditText edit = (EditText) findViewById(R.id.UIStopIDBox);
        
        edit.setOnEditorActionListener(new OnEditorActionListener(){    
        	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
            	EditText edit = (EditText) findViewById(R.id.UIStopIDBox);
            	new JsonRequest().execute("http://developer.trimet.org/ws/V1/arrivals?locIDs="+edit.getText().toString()+"&json=true&appID="+getString(R.string.appid));
            	
            	edit.getText().clear();
            	
            }    
            return false;
        }});
    }
    
    private void initList(){
		ListView view = (ListView) findViewById(R.id.UIHistoryList);
		histAdaptor=new HistStopArrayAdaptor(this, history);
		view.setAdapter(histAdaptor);
		
		Intent intent = getIntent();
		int count = intent.getIntExtra("count", 0);
		for (int i = 0; i < count; i++) {
			history.add((Stop)intent.getParcelableExtra("Stop"+i));
		}
		
		histAdaptor.notifyDataSetChanged();
               
		view.setOnItemClickListener(new OnItemClickListener() {
			  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				  Stop temp = history.get(position);
				  if (temp != null) {
					  	new JsonRequest().execute("http://developer.trimet.org/ws/V1/arrivals?locIDs="+temp.StopID+"&json=true&appID="+getString(R.string.appid));
				  }
			  }
		});
		
        // Create a ListView-specific touch listener. ListViews are given special treatment because
        // by default they handle touches for their list items... i.e. they're in charge of drawing
        // the pressed state (the list selector), handling list item clicks, etc.
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(view,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                	histAdaptor.remove(histAdaptor.getItem(position));
                                	Toast.makeText(getApplicationContext(), "Removed Stop", android.R.integer.config_shortAnimTime).show();
                                }
                                histAdaptor.notifyDataSetChanged();
                            }
                        });
        view.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        view.setOnScrollListener(touchListener.makeScrollListener());
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history_view, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	Intent parentActivityIntent = new Intent(this, Util.parents.pop());
        	parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        	//startActivity(parentActivityIntent);
            NavUtils.navigateUpTo(this, parentActivityIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public class JsonRequest extends AsyncTask<String, String, String> {
    	@Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        	Gson gson = new Gson();
        	results test = gson.fromJson(result, results.class);
        	
        	ResultSet rs = test.resultSet;
        	
    		Stop tempStop = new Stop(rs.location[0]);
        		
    		boolean histHas = false;
    		for (Stop s : history)
    			if (s.StopID == tempStop.StopID) {
    				histHas = true;
    				break;
    			}
    		
    		if (!histHas)
    			history.add(tempStop);
    		
    		Util.parents.push(getClass());
            Intent intent = new Intent(getApplicationContext(), StopView.class);
            intent.putExtra("JSON Stop", result);
            startActivity(intent);
        }
        
    }

}
