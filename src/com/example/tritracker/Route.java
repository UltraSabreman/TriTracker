package com.example.tritracker;

import java.util.ArrayList;

import com.example.tritracker.json.AllRoutesJSONResult.ResultSet;

public class Route {
	public ArrayList<Stop> Stops = new ArrayList<Stop>();
	public String Discription = "";
	public int Route = 0;
	public String Type = "";
	
	public Route(ResultSet.Route r) {
		Discription = r.desc;
		Route = r.route;
		Type = r.type;
		
		for (ResultSet.Route.Dir d: r.dir) 
			if (d != null && d.stop != null)
				for (ResultSet.Route.Dir.Stop s: d.stop)
					Stops.add(new Stop(s, d.desc));
		
	}

}
