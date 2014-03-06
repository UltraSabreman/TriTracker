package com.example.tritracker;

import com.example.tritracker.json.AllRoutesJSONResult;
import com.example.tritracker.json.ArrivalJSONResult;
import com.example.tritracker.json.MapJSONResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
//make me more specific
//import android.content.

public class Stop {
	public String Name = "Invalid Street";
	public int StopID = -1;
	public String Direction = "Up Up and Away!";
	public Date LastAccesed = null;
    public String Lines = "";

	public boolean inHistory = false;
	public boolean inFavorites = false;

	public ArrayList<Buss> Busses = new ArrayList<Buss>();

	public double Latitude = 0;
	public double Longitude = 0;

	public Stop(ArrivalJSONResult.ResultSet.Location l) {
		Name = l.desc;
		StopID = l.locid;
		Direction = l.dir;
		Latitude = l.lat;
		Longitude = l.lng;
        Lines = Util.getListOfLines(this);
	}

	public Stop(MapJSONResult.ResultSet.Location l) {
		Name = l.desc;
		StopID = l.locid;
		Direction = l.dir;
		Latitude = l.lat;
		Longitude = l.lng;
        Lines = Util.getListOfLines(this);
		/*if (l.route != null) {
			for (Route r : l.route) {
				if (r.type.compareTo("R") == 0){
					Routes.add(r.desc);//.substring(0, r.desc.indexOf(" ")));
				} else 
					Routes.add(String.valueOf(r.route));
			}
					
		}*/
	}

	public Stop(AllRoutesJSONResult.ResultSet.Route.Dir.Stop s, String des) {
		Name = s.desc;
		StopID = s.locid;
		Latitude = s.lat;
		Longitude = s.lng;
		Direction = des;
        Lines = Util.getListOfLines(this);
    }

	public Stop(int id) {
		StopID = id;
	}

	public void Update(Stop s, boolean shouldUpdateDate) {
		Name = new String(s.Name);
		StopID = s.StopID;
		Direction = new String(s.Direction);
        Lines = s.Lines;
		//inFavorites = s.inFavorites;
		//inHistory = s.inHistory;
		if (shouldUpdateDate && s.LastAccesed != null)
			LastAccesed = s.LastAccesed;

		if (Busses != null) {
			for (Buss b : s.Busses) {
				Buss tempBuss = getBuss(b);
				if (tempBuss != null)
					tempBuss.update(b);
				else
					Busses.add(new Buss(b));
			}

			for (Iterator<Buss> it = Busses.iterator(); it.hasNext(); )
				if (s.getBuss(it.next()) == null)
					it.remove();
		}
	}

	public Buss getBuss(Buss ib) {
		return getBuss(ib.SignLong);
	}

	public Buss getBuss(String name) {
		for (Buss b : Busses)
			if (b != null && b.SignLong.compareTo(name) == 0)
				return b;
		return null;
	}
}
