package com.example.tritracker.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Util;
import com.example.tritracker.activities.MainService.LocalBinder;
import com.example.tritracker.arrayadaptors.MarkerInfoWindowArrayAdaptor;
import com.example.tritracker.json.ForgroundRequestManager;
import com.example.tritracker.json.ForgroundRequestManager.ResultCallback;
import com.example.tritracker.json.Request;
import com.example.tritracker.json.MapJSONResult;
import com.example.tritracker.json.MapJSONResult.ResultSet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.example.tritracker.arrayadaptors

public class MapActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	private LocationClient mLocationClient = null;
	private Circle searchCircle = null;
	private Marker searchMarker = null;
	private MainService theService;
	private boolean bound;
	private LatLng targetPos = null;
	private ArrayList<Marker> stops = new ArrayList<Marker>();
	
	private static LatLng oldPos = null;
	
	@Override 
	public void onStart() {
		super.onStart();
		Intent intent = new Intent(this, MainService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);     
	}
	
    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.disconnect();
        if (bound) 
            unbindService(mConnection);
                
        if (searchMarker != null) {
        	oldPos = searchMarker.getPosition();
           	searchMarker.remove();
        } else
        	oldPos = null;
        
        if (searchCircle != null)
        	searchCircle.remove();  
        
        targetPos = null;
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            theService = binder.getService();
            mLocationClient.connect();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        Util.parents.push(getClass());
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Get a handle to the Map Fragment
        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setInfoWindowAdapter(new MarkerInfoWindowArrayAdaptor(getLayoutInflater()));
        
        mLocationClient = new LocationClient(this, this, this);//(this, this, this);
        map.setMyLocationEnabled(true);
       
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        	targetPos = new LatLng(extras.getDouble("lat"), extras.getDouble("lng"));

    }
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_action_bar, menu);
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
		case R.id.action_settings:
			startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {		
		GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	
        LatLng myPos;//
        if (targetPos != null) {
        	myPos = targetPos;
        }else if (oldPos != null) {
        	myPos = oldPos;
        	oldPos = null;
        } else {
            Location test = mLocationClient.getLastLocation();
        	myPos = new LatLng(test.getLatitude(), test.getLongitude());//-33.867, 151.206);
        }
        
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, targetPos != null ? 18 : 15));
        
        
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_orange);
        
       
        searchMarker = map.addMarker(new MarkerOptions()
			.icon(icon)//BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
	        .title("Search Location")
	        .snippet("From where to search for stops.")
	        .position(myPos)
	        .draggable(true));
        if (targetPos != null)
        	searchMarker.setVisible(false);
        
        
        map.setOnMarkerDragListener(new OnMarkerDragListener() {
			@Override 
			public void onMarkerDragEnd(Marker marker) {
				searchCircle.setVisible(true);
				searchCircle.setCenter(marker.getPosition());
				targetPos = null;
				getJson();
			}
			
			@Override
			public void onMarkerDrag(Marker marker) {
				
			}

			@Override 
			public void onMarkerDragStart(Marker arg0) {
				searchCircle.setVisible(false);
			}
		});
        		
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker mark) {
				if (mark != searchMarker) {
					getJson(Integer.parseInt(mark.getSnippet().substring(0,mark.getSnippet().indexOf("|"))));
				}
			}
		});
		
		map.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
			@Override
			public boolean onMyLocationButtonClick() {
		        Location test = mLocationClient.getLastLocation();
		        LatLng myPos = new LatLng(test.getLatitude(), test.getLongitude());//-33.867, 151.206);
		        
		        if(!searchMarker.isVisible())
		        	searchMarker.setVisible(true);
		        
		        searchMarker.setPosition(myPos);
				searchCircle.setCenter(myPos);
				targetPos = null;
				getJson();
				return false;
			}
		});
        
        map.setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng pos) {
		        if(!searchMarker.isVisible())
		        	searchMarker.setVisible(true);
				searchMarker.setPosition(pos);
				searchCircle.setCenter(pos);
				targetPos = null;
				getJson();
			}
		});
        
        CircleOptions circleOptions = new CircleOptions()
	        .fillColor(0x30aaaaFF)
	        .strokeColor(0xFFaaaaFF)
	        .strokeWidth(5f)
	        .center(myPos)
	        .radius(theService.getMapRadius()); 

	    // Get back the mutable Circle
        searchCircle = map.addCircle(circleOptions);
        getJson();
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	
	private void getJson(int stop) {
		Util.creatDiag(this);
		ResultCallback call = new ForgroundRequestManager.ResultCallback() { 
			public void run(Stop s) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Util.hideDiag();
					}
				});
				
				Context c = getApplicationContext();
				
				Intent tempIntent = new Intent(c, StopDetailsActivity.class);//.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				tempIntent.putExtra("stop", s);
				
				c.startActivity(tempIntent);
				theService.doUpdate(false);
			}
		};
		
		new ForgroundRequestManager(theService, call, this, getApplicationContext(), stop).start();
		
	}
	
	public void getJson() {
		for (Marker m : stops)
			m.remove();
	
		stops.clear();

		LatLng thePos = (targetPos != null ? targetPos : searchMarker.getPosition());
		
		new Request<MapJSONResult>(MapJSONResult.class, 
			new Request.JSONcallback<MapJSONResult>() {
				@Override
				public void run(final MapJSONResult r, final int error) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							proccesLocations(r, error);
						}
					});
				}
			}, 
			"http://developer.trimet.org/ws/V1/stops?ll="
			+ thePos.longitude + "," + thePos.latitude
			+ "&meters=" + theService.getMapRadius()
			+ "&showRoutes=true"
			+ "&json=true"
			+ "&appID=" + getString(R.string.appid)).start();
		
	}
	
	public void proccesLocations(MapJSONResult r, int error){
		if (r == null) 
			return;
		
		MapJSONResult.ResultSet rs = r.resultSet;
		
		if (rs.errorMessage != null) //TODO: handle me
			return;
		
		ArrayList<Stop> toAdd = new ArrayList<Stop>();
		if (rs.location == null) return;
		for (ResultSet.Location l : rs.location)
			toAdd.add(new Stop(l));

		if (toAdd.size() != 0) {
			GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			if (map == null) return;
			BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_blue);
			for (Stop s : toAdd) {
				stops.add(map.addMarker(new MarkerOptions()
					.icon(icon)
			        .title(s.Name)
			        .snippet(s.StopID+"|"+s.getService())
			        .position(new LatLng(s.Latitude, s.Longitude))
				));
			}
			
			if (targetPos != null) {
				getClosestMarker(targetPos).showInfoWindow();
			}
		}
	}
	
	public Marker getClosestMarker(LatLng pos) {
		if (stops == null || stops.size() == 0) return null;
		Marker close = stops.get(0);
		float minDist = distFrom(pos, close.getPosition());
		for (Marker m : stops) {
			float dist = distFrom(pos, m.getPosition());
			if (dist < minDist) {
				minDist = dist;
				close = m;
			}
		}
		
		return close;
	}
	
	public float distFrom (LatLng pos1, LatLng pos2) 
	{
	    double earthRadius = 3958.75;
	    double dLat = Math.toRadians(pos2.latitude-pos1.latitude);
	    double dLng = Math.toRadians(pos2.longitude-pos1.longitude);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(Math.toRadians(pos1.latitude)) * Math.cos(Math.toRadians(pos2.latitude)) *
	    Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    int meterConversion = 1609;

	    return new Float(dist * meterConversion).floatValue();
	}

}
