package com.example.tritracker.map;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Util;
import com.example.tritracker.activities.MainService;
import com.example.tritracker.activities.StopDetailsActivity;
import com.example.tritracker.json.AllRoutesJSONResult;
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
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Random;

public class MapOverlayRoutes {
    private MainService theService = null;
    private Map parentMap = null;
    private Context context = null;
    private Activity activity = null;

    public MapOverlayRoutes(Map parentMap, Context c, Activity a) {
        this.parentMap = parentMap;
        context = c;
        activity = a;
        theService = MainService.getService();

        DrawRoute(33);
    }

    public Bitmap drawStopCircle(int col) {
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


    public void DrawRoute(int RouteNum) {
        MainService.MapRouteData test = theService.getMapRoute(RouteNum);
        if (test == null) return;
        Util.print(test.Route + " | " + test.Description);

        Random rand = new Random();
        rand.setSeed(test.Route);

        int colr = rand.nextInt(255) + 1;
        int colg = rand.nextInt(255) + 1;
        int colb = rand.nextInt(255) + 1;

        int color = 0xFF000000;
        color = color | (colr << (4 * 4));
        color = color | (colg << (4 * 2));
        color = color | (colb);

        color = 0xff00aa00;

        PolylineOptions rectOptions = new PolylineOptions();
        rectOptions.color(color);
        rectOptions.geodesic(true);

        for (MainService.MapRouteData.RouteDir d: test.Directions)
            for (MainService.MapRouteData.RouteDir.RoutePart p : d.parts) {
                for (LatLng l : p.coords) {
                    parentMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(drawStopCircle(color)))//.icon(icon)//BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .position(l)
                            .flat(true));

                    rectOptions.add(l);
                }
            }

        parentMap.getMap().addPolyline(rectOptions);
    }

}
