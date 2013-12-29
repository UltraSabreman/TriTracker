package com.example.tritracker;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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

import com.example.tritracker.json.Arrival;
import com.example.tritracker.json.ResultSet;
import com.example.tritracker.json.results;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

class JsonWrapper {
	
	public ArrayList<Stop> favorites = new ArrayList<Stop>();
	public ArrayList<Stop> history = new ArrayList<Stop>();
	
	public JsonWrapper(ArrayList<Stop> fav, ArrayList<Stop> hist) {
		favorites = fav;
		history = hist;
	}
}

public class MainActivity extends Activity{
	private ArrayList<Stop> favorites = new ArrayList<Stop>();
	private ArrayList<Stop> history = new ArrayList<Stop>();
	FavStopArrayAdaptor favAdaptor;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);   
        Util.parents.push(getClass());
        
       //File t = new File(getString(R.string.data_path));
       //t.delete();
        
        readData();
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
        ListView view = (ListView) findViewById(R.id.UIStopList);

        favAdaptor=new FavStopArrayAdaptor(this, favorites);       
        view.setAdapter(favAdaptor);
               
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
                                	favAdaptor.remove(favAdaptor.getItem(position));
                                	Toast.makeText(getApplicationContext(), "Removed Stop", android.R.integer.config_shortAnimTime).show();
                                }
                                favAdaptor.notifyDataSetChanged();
                            }
                        });
        view.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        view.setOnScrollListener(touchListener.makeScrollListener());
    }
    
    private void readData() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(getString(R.string.data_path))));
			
			String fileContents = "";
			String line;
			while ((line = br.readLine()) != null){
				fileContents += line;
			}
			
			Gson gson = new Gson();
			JsonWrapper wrap = gson.fromJson(fileContents, JsonWrapper.class);
			
			favorites = new ArrayList<Stop>(wrap.favorites);
			history = new ArrayList<Stop>(wrap.history);
			
			//TODO dtopthe busses from being written
			br.close();
		}catch (JsonSyntaxException e) { 
			
		}catch (FileNotFoundException e) {
			
		}catch (IOException e) {  
		
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private void dumpData(){
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(openFileOutput(getString(R.string.data_path), Context.MODE_PRIVATE)));
			
			Gson gson = new Gson();
			JsonWrapper wrap = new JsonWrapper(favorites, history);
			String data = gson.toJson(wrap);
			
			bw.write(data);		
			bw.close();
		}catch (JsonSyntaxException e) {
			
		}catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch (IOException e2) {
			// TODO Auto-generated catch block
						e2.printStackTrace();
		} 		
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	dumpData();
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
                
            	Toast.makeText(getApplicationContext(), "Not implimented yet. Sorry!", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_settings:            	 
        
                Toast.makeText(getApplicationContext(), "Not implimented yet. Sorry!", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_history:
            	Intent intent = new Intent(this, HistoryView.class);
                intent.putExtra("count", history.size());
                int i = 0;
                
                for (Stop s : history)
                	intent.putExtra("Stop"+(i++), s);
                
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
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
    	
    		dumpData();
    		
            Intent intent = new Intent(getApplicationContext(), StopView.class);
            intent.putExtra("JSON Stop", result);
            Util.parents.push(getClass());
            startActivity(intent);

        }
        
    }
}

