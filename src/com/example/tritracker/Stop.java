package com.example.tritracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.example.tritracker.json.Location;
import com.google.gson.annotations.Expose;
//make me more specific
//import android.content.

public class Stop  {
	@Expose public String Name = "Invalid Street";
	@Expose public int StopID = -1;
	@Expose public String Direction = "Up Up and Away!";
	@Expose public Date LastAccesed = null;

	@Expose public ArrayList<Buss> Busses = new ArrayList<Buss>();

	public Stop(Location l) {
		Name = l.desc;
		StopID = l.locid;
		Direction = l.dir;
	}

	public void Update(Stop s, boolean shouldUpdateDate) {
		Name = new String(s.Name);
		StopID = s.StopID;
		Direction = new String(s.Direction);
		if (shouldUpdateDate && s.LastAccesed != null)
			LastAccesed = s.LastAccesed;
		
		if (s.Busses == null)
			Busses = null;
		else if(Busses != null) {
			for (Buss b : s.Busses) {
				Buss tempBuss = getBuss(b);
				if (tempBuss != null)
					tempBuss.update(b);
				else
					Busses.add(new Buss(b));
			}
		}
	}

	public Buss getBuss(Buss ib) {
		for (Buss b : Busses)
			if (b != null && b.ScheduledTime.compareTo(ib.ScheduledTime) == 0)
				return b;
		return null;
	}
	
	public void clearArivals() {
		for (Iterator<Buss> it = Busses.iterator(); it.hasNext(); )
			if (Util.getBussMinutes(it.next()) <= 0)
	            it.remove();
	}
}
