package com.example.tritracker.map;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.example.tritracker.Stop;
import com.example.tritracker.Timer;
import com.example.tritracker.activities.MainService;
import com.example.tritracker.arrayadaptors.MarkerInfoWindowArrayAdaptor;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.ConnectException;

public class Map implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	private MainService theService = null;
	private GoogleMap map = null;
	private Context cont = null;
	private Activity act = null;
	private LocationClient locationClient = null;

	private MapOverlaySearch searchLayer = null;
	private MapOverlayTracking trackingLayer = null;
    private MapOverlayRoutes routeLayer = null;

	private boolean connected = false;

	public final int zoomLevel = 15;

	public boolean isConnected() { return connected; }
	public GoogleMap getMap() { return map; }

	public Map(MapFragment frag, Context c, Activity a) {
		map = frag.getMap();
		cont = c;
		act = a;
		theService = MainService.getService();

		// Get a handle to the Map Fragment
		GoogleMap map = frag.getMap();
		if (map != null) {
			map.setInfoWindowAdapter(new MarkerInfoWindowArrayAdaptor(a.getLayoutInflater(), c));

			locationClient = new LocationClient(cont, this, this);
			map.setMyLocationEnabled(true);
			locationClient.connect();

			theService.sub("map tick", new Timer.onUpdate() {
				@Override
				public void run() {
					act.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							update();
						}
					});
				}
			});
		}
	}

	public void showStops(Stop s) {
		if (searchLayer == null)
			searchLayer = new MapOverlaySearch(this, cont, act);

		searchLayer.searchForStops(s);
	}

	public void RouteLayerDraw(String routes) {
		routeLayer.DrawRoute(routes);
	}

	public void TrackingLayerDraw(Stop s, int route, int blockid) {
		setCameraPos(new LatLng(s.Latitude, s.Longitude), zoomLevel);

		showStops(s);
		trackingLayer.DoTransition(s, route, blockid);
	}

	public void TrackingLayerSwitchBuss(int blockid) {
		if (trackingLayer != null)
			trackingLayer.switchBuss(blockid);
	}

	public void setSearchLayerEnabled(boolean should) throws ConnectException {
		if (!connected) throw new ConnectException("Not connected to play services. Please wait.");
		if (should && searchLayer == null)
			searchLayer = new MapOverlaySearch(this, cont, act);
		else if(!should && searchLayer != null) {
			searchLayer.clearAll();
			searchLayer = null;
		}
	}

	public void setTrackingLayerEnabled(boolean should) throws ConnectException {
		if (!connected) throw new ConnectException("Not connected to play services. Please wait.");
		if (should && trackingLayer == null)
			trackingLayer = new MapOverlayTracking(this, cont, act);
		else if(!should && trackingLayer != null) {
			trackingLayer.clearAll();
			trackingLayer = null;
		}
	}

    public void setRouteLayerEnabled(boolean should) throws ConnectException {
        if (!connected) throw new ConnectException("Not connected to play services. Please wait.");
        if (should && routeLayer == null)
            routeLayer = new MapOverlayRoutes(this, cont, act);
        else if(!should && trackingLayer != null) {
            //routeLayer.clearAll();
            routeLayer = null;
        }
    }


	public void setCameraPos(LatLng pos, int zoom) {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoom));
	}

	public void gotoLocation(LatLng pos, int zoom) {
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, zoom));
	}


	public void update() {
		if (searchLayer != null)
			searchLayer.update();
		if (trackingLayer != null)
			trackingLayer.update();
	}

	@Override
	public void onDisconnected() {
		connected = false;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		connected = false;

	}

	@Override
	public void onConnected(Bundle arg0) {
		connected = true;
	}


	public Marker addMarker(MarkerOptions m) {
		return map.addMarker(m);
	}


	/*public Marker getClosestStop(LatLng pos) {
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
	}*/



	public float distFrom(LatLng pos1, LatLng pos2) {

		double earthRadius = 3958.75;
		double dLat = Math.toRadians(pos2.latitude - pos1.latitude);
		double dLng = Math.toRadians(pos2.longitude - pos1.longitude);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
				Math.cos(Math.toRadians(pos1.latitude)) * Math.cos(Math.toRadians(pos2.latitude)) *
						Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		int meterConversion = 1609;

		return (float) (dist * (float) meterConversion);
	}

	public LatLng getMyPos() {
		Location searchLayer = locationClient.getLastLocation();
		return new LatLng(searchLayer.getLatitude(), searchLayer.getLongitude());
	}

}
