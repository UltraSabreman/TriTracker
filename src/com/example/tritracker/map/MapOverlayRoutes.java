package com.example.tritracker.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.tritracker.RouteNamer;
import com.example.tritracker.activities.MainService;
import com.example.tritracker.json.XmlRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MapOverlayRoutes {
    private MainService theService = null;
    private Map parentMap = null;
    private Context context = null;
    private Activity activity = null;
	private ArrayList<MapRouteData> mapRoutes = new ArrayList<MapRouteData>();

	ArrayList<Polyline> routes = new ArrayList<Polyline>();
	ArrayList<Marker> stops = new ArrayList<Marker>();


	public MapOverlayRoutes(Map parentMap, Context c, Activity a) {
        this.parentMap = parentMap;
        context = c;
        activity = a;
        theService = MainService.getService();

    }

    public void DrawRoute(String strRoutes) {
	    List<String> routeList = Arrays.asList(strRoutes.split(","));
	    makeRoutes(routeList);

	    for (MapRouteData r : mapRoutes) {
	        if (r == null) return;

	        int color = RouteNamer.getColor(r.Route);

	        for (MapRouteData.RouteDir d: r.Directions)
	            for (MapRouteData.RouteDir.RoutePart p : d.parts) {
		            PolylineOptions rectOptions = new PolylineOptions();
		            rectOptions.color(color);
		            rectOptions.geodesic(true);

	                for (LatLng l : p.coords) {
	                    rectOptions.add(l);
	                }

		            routes.add(parentMap.getMap().addPolyline(rectOptions));
	            }

		    /*ArrayList<AllRoutesJSONResult.ResultSet.Route> t = theService.getRoutes();
		    for (AllRoutesJSONResult.ResultSet.Route route : t) {
			    if (route.route != r.Route) continue;
			    for (AllRoutesJSONResult.ResultSet.Route.Dir d : route.dir)
				    for (AllRoutesJSONResult.ResultSet.Route.Dir.Stop s : d.stop)
						stops.add(parentMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(drawStopCircle(color)))
                            .position(new LatLng(s.lat, s.lng))
                            .flat(true)));
		    }*/
	    }

    }

	public void clearAll() {
		for (Marker m: stops)
			m.remove();
		stops.clear();

		for (Polyline p: routes)
			p.remove();
		routes.clear();
	}

	private Bitmap drawStopCircle(int col) {
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
	}
	private boolean keepRoute(int route) {
		boolean flag = false;
		for (MapRouteData r: mapRoutes)
			if (r.Route == route) {
				r.keep = true;
				flag = true;
			}
		return flag;
	}


	private void makeRoutes(List<String> routes) {
		mapRoutes.clear();
		try {
			XmlRequest r = theService.getMapRoutes();
			final Field field = String.class.getDeclaredField("value");
			field.setAccessible(true);

			for (XmlRequest.document.placemark p : r.Document.BusRoutes) {
				String Route = p.RouteInfo.InfoList.get(0).Value.trim();
				if (!routes.contains(Route)) continue;

				MapRouteData temp = new MapRouteData();
				temp.Route = Integer.valueOf(Route);
				temp.Description = p.RouteInfo.InfoList.get(2).Value.trim();
				temp.Type = p.RouteInfo.InfoList.get(6).Value.trim();

				int dir = Integer.valueOf(p.RouteInfo.InfoList.get(1).Value.trim());
				String dirDesc = p.RouteInfo.InfoList.get(4).Value.trim();

				if (temp.getDir(dir) == null) {
					MapRouteData.RouteDir tr = temp.new RouteDir();
					tr.Direction = dir;
					tr.DirectionDesc = dirDesc;

					temp.Directions.add(tr);
				}
				for (XmlRequest.document.placemark.MulGeo.LineString l : p.RouteCoordinates.RouteSections) {
					MapRouteData.RouteDir.RoutePart tempPart = temp.getDir(dir).new RoutePart();


					boolean inLng = true;
					StringBuilder lat = null;
					StringBuilder lng = null;

					final char[] chars = (char[]) field.get(l.Coordinates.replaceAll("^\\s+ | \\s+&", ""));
					final int len = chars.length;
					for (int i = 0; i < len; i++) {
						char curChar = chars[i];

						if (curChar == ' ') {
							if (lat != null) {
								tempPart.coords.add(new LatLng(Double.valueOf(lat.toString()), Double.valueOf(lng.toString())));
								lng = null;
								lat = null;
								inLng = true;
							} else
								break;
							//TODO improve error handeling.
						} else if (curChar == ',')
							inLng = false;
						else if (curChar != ' ') {
							if (inLng) {
								if (lng == null)
									lng = new StringBuilder();
								lng.append(curChar);
							} else {
								if (lat == null)
									lat = new StringBuilder();
								lat.append(curChar);
							}
						}
					}
					if (lat != null && lng != null)
						//this gets the last set in the list
						tempPart.coords.add(new LatLng(Double.valueOf(lat.toString()), Double.valueOf(lng.toString())));
					temp.getDir(dir).parts.add(tempPart);
				}

				mapRoutes.add(temp);
			}

		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		/*for (Iterator<MapRouteData> r = mapRoutes.iterator(); r.hasNext();)
			if (!r.next().keep)
				r.remove();*/
	}


	public class MapRouteData {
		public int Route;
		public String Description;
		public String Type;
		public boolean keep = false;

		public ArrayList<RouteDir> Directions = new ArrayList<RouteDir>();
		public RouteDir getDir(int i) {
			for (RouteDir r : Directions)
				if (r.Direction == i)
					return r;
			return null;
		}

		public class RouteDir {
			public int Direction;
			public String DirectionDesc;
			public ArrayList<RoutePart> parts = new ArrayList<RoutePart>();


			public class RoutePart {
				public ArrayList<LatLng> coords = new ArrayList<LatLng>();
			}
		}
	}
}
