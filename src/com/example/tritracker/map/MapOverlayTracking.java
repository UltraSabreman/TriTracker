package com.example.tritracker.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.example.tritracker.R;
import com.example.tritracker.RouteNamer;
import com.example.tritracker.Stop;
import com.example.tritracker.Timer;
import com.example.tritracker.activities.MainService;
import com.example.tritracker.json.BussesJSONResult;
import com.example.tritracker.json.Request;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MapOverlayTracking {
	private MainService theService = null;
	private Map parentMap = null;
	private Context context = null;
	private Activity activity = null;

	private String bussRouteFilter = "";

	private Stop curStop = null;
	private int curBlock = -1;

	private boolean drawing = false;

	private ArrayList<BusMarker> buses = new ArrayList<BusMarker>();

	public MapOverlayTracking(Map parentMap, Context c, Activity a) {
		this.parentMap = parentMap;
		context = c;
		activity = a;
		theService = MainService.getService();
	}

	public void update() {
		Draw(bussRouteFilter, false);
	}

	public void switchBuss(int blockid) {
		curBlock = blockid;
		//if (moveToCurBuss()) return;
		Draw(null, true);
		moveToCurBuss();
	}

	public void clearAll() {
		for (BusMarker m : buses)
			m.maker.remove();

		buses.clear();
	}


	public void DoTransition(Stop s, final int route, int block) {
		curBlock = block;
		curStop = s;

		Draw(String.valueOf(route), false);

		final Timer delay = new Timer(3);
			delay.addCallBack("", new Timer.onUpdate() {
				@Override
				public void run() {
					if (drawing) return;
					delay.stopTimer();
					moveToCurBuss();
				}
			});

		delay.restartTimer();
	}

	private boolean moveToCurBuss() {
		for (BusMarker m: buses) {
			if (m.blockid == curBlock) {
				parentMap.gotoLocation(m.maker.getPosition(), parentMap.zoomLevel);
				return true;
			}
		}
		return false;
	}

	public void Draw(final String InBussRouteFilter, final boolean move) {
		if (InBussRouteFilter != null)
			bussRouteFilter = InBussRouteFilter;

		drawing = true;
		new Request<BussesJSONResult>(BussesJSONResult.class,
				new Request.JSONcallback<BussesJSONResult>() {
					@Override
					public void run(final BussesJSONResult r, String s,  final int error) {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								parseBusses(r);
								if (move)
									moveToCurBuss();
								drawing = false;
							}
						});
					}
				},
				"http://developer.trimet.org/beta/v2/vehicles?routes=" + bussRouteFilter
						+ "&appID=" + activity.getString(R.string.appid)).start();
	}

	private void parseBusses(BussesJSONResult r) {
		if (r == null || r.resultSet == null || r.resultSet.vehicle == null) return;

		for (BussesJSONResult.ResultSet.Vehicle v : r.resultSet.vehicle) {
			LatLng pos = new LatLng(v.latitude, v.longitude);

			BusMarker bus = null;
			for (BusMarker m: buses)
				if (m.vehicleID == v.vehicleID) {
					bus = m;
					break;
				}

			if (bus == null) {
				String id = RouteNamer.getShortName(v.routeNumber);
                int color = RouteNamer.getColor(v.routeNumber);

				BusMarker tempBuss = new BusMarker();
					tempBuss.maker = parentMap.getMap().addMarker(new MarkerOptions()
							.icon(BitmapDescriptorFactory.fromBitmap(drawBussCircle(id, color)))
							.anchor(0.5f, 0.5f)
							.rotation((float) v.bearing)
							.position(pos)
							.flat(true));
					tempBuss.vehicleID = v.vehicleID;
					tempBuss.updated = true;
					tempBuss.blockid = v.blockID;

				buses.add(tempBuss);
			} else {
				bus.maker.setPosition(pos);
				bus.maker.setRotation((float) v.bearing);
				bus.updated = true;
				bus.blockid = v.blockID;
			}

		}

		for (Iterator<BusMarker> it = buses.iterator(); it.hasNext();) {
			BusMarker temp = it.next();
			if (!temp.updated)
				it.remove();
			else
				temp.updated = false;
		}
	}

	private Bitmap drawBussCircle(String ID, int col) {
		Bitmap result = Bitmap.createBitmap(100, 120, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(result);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setTextSize(40);
		paint.setFakeBoldText(true);

		canvas.drawCircle(result.getWidth() / 2, (result.getHeight() + 20) / 2, 50, paint);

		Path path = new Path();
			path.moveTo(result.getWidth() / 2, 0);
			path.lineTo(result.getWidth() / 2 + 20, 25);
			path.lineTo(result.getWidth() / 2 - 20, 25);
		path.close();
		canvas.drawPath(path, paint);

		paint.setColor(col);
		canvas.drawCircle(result.getWidth() / 2, (result.getHeight() + 20) / 2, 48, paint);

		paint.setColor(Color.BLACK);
		Rect bounds = new Rect();
		paint.getTextBounds(ID, 0, ID.length(), bounds);

		canvas.drawText(ID, result.getWidth() / 2 - bounds.width() / 2, result.getHeight() / 2 + (bounds.height() + 20) / 2, paint);

		return result;
	}

	public BusMarker getClosestBuss(LatLng pos) {
		if (buses == null || buses.size() == 0) return null;

		BusMarker close = buses.get(0);

		float minDist = parentMap.distFrom(pos, close.maker.getPosition());

		for (BusMarker m : buses) {
			float dist = parentMap.distFrom(pos, m.maker.getPosition());
			if (dist < minDist) {
				minDist = dist;
				close = m;
			}
		}

		return close;
	}

	private class BusMarker {
		public Marker maker;
		public int vehicleID;
		public int blockid;
		public boolean updated = false;
	}

}
