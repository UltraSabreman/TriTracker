package com.example.tritracker.map;

public class MapOverlayRoutes {

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

}
