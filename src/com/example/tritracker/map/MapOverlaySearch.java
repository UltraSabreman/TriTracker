package com.example.tritracker.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Util;
import com.example.tritracker.activities.MainService;
import com.example.tritracker.activities.StopDetailsActivity;
import com.example.tritracker.json.ForgroundRequestManager;
import com.example.tritracker.json.MapJSONResult;
import com.example.tritracker.json.Request;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class MapOverlaySearch {
	private ArrayList<Marker> stops = new ArrayList<Marker>();
	private MainService theService = null;
	private Map parentMap = null;
	private Context context = null;
	private Activity activity = null;

	private Marker searchMarker = null;
	private Circle searchCircle = null;

	private LatLng oldPos = null;

	public MapOverlaySearch(Map parentMap, Context c, Activity a) {
		this.parentMap = parentMap;
		context = c;
		activity = a;
		theService = MainService.getService();

		DrawLayer(null);
	}

	public void update() {

	}

	public void clearAll() {
		for (Marker m : stops)
			m.remove();

		stops.clear();
		searchCircle.remove();
		searchMarker.remove();
	}

	public void DrawLayer(final LatLng targetPos) {
		LatLng searchPos = null;
		if (targetPos == null)
			searchPos = parentMap.getMyPos();
		else
			searchPos = targetPos;

		oldPos = targetPos;

		GoogleMap map = parentMap.getMap();

		LatLng finalSearchPos = searchPos;
		if (searchMarker == null) {
			searchMarker = map.addMarker(new MarkerOptions()
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_orange))
					.title("Search Point")
					.position(finalSearchPos)
					.draggable(true));
		} else
			searchMarker.setPosition(finalSearchPos);
		searchMarker.setVisible(targetPos != null);

		if (searchCircle == null) {
			CircleOptions circleOptions = new CircleOptions()
					.fillColor(0x30aaaaFF)
					.strokeColor(0xFFaaaaFF)
					.strokeWidth(5f)
					.center(finalSearchPos)
					.radius(theService.getMapRadius());
			searchCircle = map.addCircle(circleOptions);
		} else
			searchCircle.setCenter(finalSearchPos);

		map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
			@Override
			public void onMarkerDragEnd(Marker marker) {
				searchForStops(null);
			}

			@Override
			public void onMarkerDrag(Marker marker) {
				searchCircle.setCenter(marker.getPosition());
			}

			@Override
			public void onMarkerDragStart(Marker arg0) {}
		});

		map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker mark) {
				if (stops.contains(mark)) {
					viewStopInDetail(Integer.parseInt(mark.getSnippet().substring(0, mark.getSnippet().indexOf("|"))));
				}
			}
		});

		map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
			@Override
			public boolean onMyLocationButtonClick() {
				if (!searchMarker.isVisible())
					searchMarker.setVisible(true);

				LatLng pos = parentMap.getMyPos();

				searchMarker.setPosition(pos);
				searchCircle.setCenter(pos);

				searchForStops(null);

				return false;
			}
		});

		map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick(LatLng pos) {
				searchMarker.setVisible(true);
				searchMarker.setPosition(pos);
				searchCircle.setCenter(pos);
				searchForStops(null);
			}
		});

		parentMap.setCameraPos(searchPos, parentMap.zoomLevel);
	}

	public void searchForStops(final Stop s) {
		LatLng thePos;
		final boolean active = (s != null);
		if (active)
			thePos = new LatLng(s.Latitude, s.Longitude);
		else
			thePos = searchMarker.getPosition();

		parentMap.setCameraPos(thePos, parentMap.zoomLevel);

		if (!active)
			Util.createSpinner(activity);
		new Request<MapJSONResult>(MapJSONResult.class,
				new Request.JSONcallback<MapJSONResult>() {
					@Override
					public void run(final MapJSONResult r, final String str,  final int error) {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								proccesLocations(s, r, error);
								if (!active)
									Util.hideSpinner();
							}
						});
					}
				},
				"http://developer.trimet.org/ws/V1/stops?showRoutes=true&json=true"
						+ "&appID=" + activity.getString(R.string.appid)
						+ (!active
						? "&ll=" + thePos.longitude + "," + thePos.latitude + "&meters=" + theService.getMapRadius()
						: "&ll=" +  s.Longitude + "," + s.Latitude + "&meters=10"
				)).start();

	}

	public void proccesLocations(Stop stop, MapJSONResult r, int error) {
		if (r == null)
			return;

		for (Marker m: stops)
			m.remove();
		stops.clear();

		MapJSONResult.ResultSet rs = r.resultSet;

		if (rs.errorMessage != null) //tODO: handle me
			return;

		if (rs.location == null) return;

		ArrayList<Stop> toAdd = new ArrayList<Stop>();
		for (MapJSONResult.ResultSet.Location l : rs.location)
			toAdd.add(new Stop(l));

		if (toAdd.size() != 0) {
			BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_blue);
			for (Stop s : toAdd) {
				if (stop != null && stop.StopID != s.StopID) continue;

				stops.add(parentMap.getMap().addMarker(new MarkerOptions()
						.icon(icon)
						.title(s.Name)
						.snippet(s.StopID + "|" + Util.getListOfLines(s, true))
						.position(new LatLng(s.Latitude, s.Longitude))
				));
			}

			if (stop != null) {
				stops.get(0).showInfoWindow();
			}
		}
	}


	private void viewStopInDetail(int stop) {
		Util.createSpinner(activity);
		ForgroundRequestManager.ResultCallback call = new ForgroundRequestManager.ResultCallback() {
			public void run(Stop s) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Util.hideSpinner();
					}
				});

				Intent tempIntent = new Intent(context, StopDetailsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				tempIntent.putExtra("stop", s.StopID);

				activity.startActivity(tempIntent);
				theService.doUpdate(false);
			}
		};

		new ForgroundRequestManager(call, activity, context, stop).start();
	}


}
