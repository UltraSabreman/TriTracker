package com.example.tritracker;

import java.util.HashMap;
import java.util.Random;

public class RouteNamer {
    static private HashMap<Integer, String> shortNames = new HashMap<Integer, String>();
    static private HashMap<Integer, String> medNames = new HashMap<Integer, String>();
    static private HashMap<Integer, Integer> colors = new HashMap<Integer, Integer>();

    static public void init() {
        //green line
        shortNames.put(200, "MAX");
        medNames.put(200, "Green-Line");
        colors.put(200, 0xFF00DD00);

        //Yellow Line
        shortNames.put(190, "MAX");
        medNames.put(190, "Yellow-Line");
        colors.put(190, 0xFFDDDD00);

        //Red Line
        shortNames.put(90, "MAX");
        medNames.put(90, "Red-Line");
        colors.put(90, 0xFFDD0000);

        //Blue Line
        shortNames.put(100, "MAX");
        medNames.put(100, "Blue-Line");
        colors.put(100, 0xFF0000DD);

        //StreetCar
        shortNames.put(193, "SC");
        medNames.put(193, "NS-Line");
        shortNames.put(194, "SC");
        medNames.put(194, "CL-Line");

        //Tram
        shortNames.put(208, "AT");
        medNames.put(208, "Tram");

        //WES
        shortNames.put(203, "CR");
        medNames.put(203, "WES-Rail");
    }

    static public Boolean hasColor(int route) {
        return colors.containsKey(route);
    }

    static public String getShortName(int route) {
        if (shortNames.containsKey(route))
            return shortNames.get(route);
        else
            return String.valueOf(route);
    }

    static public String getMedName(int route) {
        if (medNames.containsKey(route))
            return medNames.get(route);
        else
            return getShortName(route);
    }

    static public int getColor(int route) {
        if (colors.containsKey(route))
            return colors.get(route);
        else {
            Random rand = new Random();
            rand.setSeed(route);

            int colr = rand.nextInt(255) + 1;
            int colg = rand.nextInt(255) + 1;
            int colb = rand.nextInt(255) + 1;

            int color = 0xFF000000;
            color |= (colr << (4 * 4));
            color |= (colg << (4 * 2));
            color |= (colb);

            return color;
        }

    }
}
