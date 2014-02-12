package com.example.tritracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;

import com.example.tritracker.activities.MainService;
import com.example.tritracker.activities.StopDetailsActivity;
import com.example.tritracker.arrayadaptors.MarkerInfoWindowArrayAdaptor;
import com.example.tritracker.json.BussesJSONResult;
import com.example.tritracker.json.ForgroundRequestManager;
import com.example.tritracker.json.MapJSONResult;
import com.example.tritracker.json.Request;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Random;

public class MapWorker implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	private MainService theService = null;
	private GoogleMap map = null;
	private Context cont = null;
	private Activity act = null;
	private LocationClient locationClient = null;

	private ArrayList<Marker> stops = new ArrayList<Marker>();
	private ArrayList<Marker> busses = new ArrayList<Marker>();

	private Marker searchMarker = null;
	private Circle searchCircle = null;

	private MapType whatToDo;

	public enum MapType {Search, Tracking, Overview};

	private LatLng oldPos = null;
	private String trackFilter = null;

	private boolean connected = false;

	private int zoomLevel = 15;

	private Stop curStop = null;
	private int curBlock = -1;

	public boolean isConnected() { return connected; }

	//TODO: makeit display current buss position and stop positions. On click, take to large map with more info
	//TODO: Make map actualy safe and not crash if auth failer/connection failer

	public MapWorker(MapFragment frag, Context c, Activity a, MapType type) {
		map = frag.getMap();
		cont = c;
		act = a;
		whatToDo = type;
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

	public void setCameraPos(LatLng pos, int zoom) {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoom));
	}

	public void gotoLocation(LatLng pos, int zoom) {
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, zoom));
	}


	public void update() {
		if(whatToDo == MapType.Tracking)
			DrawTrackingLayer(trackFilter);
	}

	public void update(int i) {
		curBlock = i;
		update();
	}



	public boolean toggleLocation() {
		if (map.isMyLocationEnabled())
			map.setMyLocationEnabled(false);
		else
			map.setMyLocationEnabled(true);

		return map.isMyLocationEnabled();
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

	public void DrawOverviewTransition(final Stop s, final int route, int block) {
		curStop = s;
		curBlock = block;
		setCameraPos(new LatLng(s.Latitude, s.Longitude), zoomLevel);
		searchForStops(s, false);
		//DELAY TIME
		final Timer test = new Timer(3);
			test.addCallBack("moveCam", new Timer.onUpdate() {
				@Override
				public void run() {
					test.stopTimer();
					DrawTrackingLayer(String.valueOf(route));
				}
			});
			test.restartTimer();
	}

	public void DrawSearchLayer(final LatLng targetPos) {
		LatLng searchPos = null;
		if (targetPos == null)
			searchPos = getMyPos();
		else
			searchPos = targetPos;

		oldPos = targetPos;


		LatLng finalSearchPos = searchPos;
		if (searchMarker == null) {
			searchMarker = map.addMarker(new MarkerOptions()
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_orange))
					.snippet("Search for stops from here.")
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
				searchForStops(null, targetPos == null);
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

				LatLng pos = getMyPos();

				searchMarker.setPosition(pos);
				searchCircle.setCenter(pos);

				searchForStops(null, targetPos == null);

				return false;
			}
		});

		map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick(LatLng pos) {
				searchMarker.setVisible(true);
				searchMarker.setPosition(pos);
				searchCircle.setCenter(pos);
				searchForStops(null, targetPos == null);
			}
		});

		setCameraPos(searchPos, zoomLevel);
		searchForStops(null, targetPos == null);
	}

	/*public Bitmap drawStopCircle(int col) {
		Bitmap result = Bitmap.createBitmap(25, 25, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(result);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);

		canvas.drawCircle(result.getWidth() / 2, result.getHeight() / 2, 12.5f, paint);

		paint.setColor(col);
		canvas.drawCircle(result.getWidth() / 2, result.getHeight() / 2, 12, paint);

		return result;
	}*/


	/*public void DrawRoute(int RouteNum) {
		AllRoutesJSONResult.ResultSet.Route test = theService.getRouteByNumber(RouteNum);
		if (test == null) return;

		PolylineOptions rectOptions = new PolylineOptions();
		rectOptions.color(0xff00aa00);
		rectOptions.geodesic(true);

		for (AllRoutesJSONResult.ResultSet.Route.Dir d : test.dir)
			for (AllRoutesJSONResult.ResultSet.Route.Dir.Stop s : d.stop) {
				LatLng pos = new LatLng(s.lat, s.lng);

				map.addMarker(new MarkerOptions()
						.icon(BitmapDescriptorFactory.fromBitmap(drawStopCircle(0xff00aa00)))//.icon(icon)//BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
						.position(pos)
						.flat(true));

				rectOptions.add(pos);
			}

		map.addPolyline(rectOptions);
	}*/

	public void DrawTrackingLayer(String busstr) {
		if (trackFilter == null)
			trackFilter = busstr;

		new Request<BussesJSONResult>(BussesJSONResult.class,
				new Request.JSONcallback<BussesJSONResult>() {
					@Override
					public void run(final BussesJSONResult r, final int error) {
						act.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								drawBusses(r);

								if (curStop != null) {
									Marker m = getClosestBuss(new LatLng(curStop.Latitude,curStop.Longitude));
									if (m != null)
										gotoLocation(m.getPosition(), zoomLevel);
								}
							}
						});
					}
				},
				"http://developer.trimet.org/beta/v2/vehicles?"
						+ (busstr != null ? "routes=" + busstr : "")
						+ "&appID=" + act.getString(R.string.appid)).start();
	}

	private void drawBusses(BussesJSONResult r) {
		if (r == null) return;

		Random rand = new Random();
		for (BussesJSONResult.ResultSet.Vehicle v : r.resultSet.vehicle) {
			if (curBlock != -1 && curBlock != v.blockID) continue;
			LatLng pos = new LatLng(v.latitude, v.longitude);
			String id;

			if (v.signMessage != null) {
				int end = v.signMessage.indexOf(" ");
				if (end == -1)
					id = String.valueOf(v.routeNumber);
				else
					id = v.signMessage.substring(0, end);
			} else
				id = String.valueOf(v.routeNumber);

			rand.setSeed(v.routeNumber);

			int colr = rand.nextInt(255) + 1;
			int colg = rand.nextInt(255) + 1;
			int colb = rand.nextInt(255) + 1;

			int color = 0xFF000000;
			color = color | (colr << (4 * 4));
			color = color | (colg << (4 * 2));
			color = color | (colb);


			busses.add(map.addMarker(new MarkerOptions()
					.icon(BitmapDescriptorFactory.fromBitmap(drawBussCirle(id, color)))
					.anchor(0.5f, 0.5f)
					.rotation((float) v.bearing)
					.position(pos)
					.flat(true)));

		}
	}

	private Bitmap drawBussCirle(String ID, int col) {
		Bitmap result = Bitmap.createBitmap(100, 120, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(result);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setTextSize(40);
		paint.setFakeBoldText(true);

		canvas.drawCircle(result.getWidth() / 2, (result.getHeight() + 20) / 2, 50, paint);

		paint.setColor(col);
		canvas.drawCircle(result.getWidth() / 2, (result.getHeight() + 20) / 2, 48, paint);

		paint.setColor(Color.BLACK);
		Rect bounds = new Rect();
		paint.getTextBounds(ID, 0, ID.length(), bounds);

		canvas.drawText(ID, result.getWidth() / 2 - bounds.width() / 2, result.getHeight() / 2 + (bounds.height() + 20) / 2, paint);

		Point t = new Point();
		t.set(result.getWidth() / 2, 0);

		Path path = new Path();
		path.moveTo(result.getWidth() / 2, 0);
		path.lineTo(result.getWidth() / 2 + 20, 25);
		path.lineTo(result.getWidth() / 2 - 20, 25);
		path.close();

		canvas.drawPath(path, paint);


		return result;
	}

	private void viewStopInDetail(int stop) {
		Util.createSpinner(act);
		ForgroundRequestManager.ResultCallback call = new ForgroundRequestManager.ResultCallback() {
			public void run(Stop s) {
				act.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Util.hideSpinner();
					}
				});

				Intent tempIntent = new Intent(cont, StopDetailsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				tempIntent.putExtra("stop", s.StopID);

				cont.startActivity(tempIntent);
				theService.doUpdate(false);
			}
		};

		new ForgroundRequestManager(call, act, cont, stop).start();
	}

	public void searchForStops(final Stop s, final boolean spin) {
		LatLng thePos;
		if (s != null)
			thePos = new LatLng(s.Latitude, s.Longitude);
		else
			thePos = searchMarker.getPosition();

		if (spin)
			Util.createSpinner(act);
		new Request<MapJSONResult>(MapJSONResult.class,
				new Request.JSONcallback<MapJSONResult>() {
					@Override
					public void run(final MapJSONResult r, final int error) {
						act.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								proccesLocations(s, r, error);
								if(spin)
									Util.hideSpinner();
							}
						});
					}
				},
				"http://developer.trimet.org/ws/V1/stops?showRoutes=true&json=true"
					+ "&appID=" + act.getString(R.string.appid)
				    + (s == null
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

				stops.add(map.addMarker(new MarkerOptions()
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

	public Marker getClosestStop(LatLng pos) {
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

	public Marker getClosestBuss(LatLng pos) {
		if (busses == null || busses.size() == 0) return null;

		Marker close = busses.get(0);

		float minDist = distFrom(pos, close.getPosition());

		for (Marker m : busses) {
			float dist = distFrom(pos, m.getPosition());
			if (dist < minDist) {
				minDist = dist;
				close = m;
			}
		}

		return close;
	}

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
		Location test = locationClient.getLastLocation();
		return new LatLng(test.getLatitude(), test.getLongitude());
	}

}
