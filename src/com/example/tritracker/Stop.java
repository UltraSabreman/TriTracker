package com.example.tritracker;
import java.util.*; //make me more specific
//import android.content.

import com.example.tritracker.json.Location;

public class Stop  {
	public String Name = "Invalid Street";
	public int StopID = -1;
	public String Direction = "Up Up and Away!";
	
	public ArrayList<Buss> BussLines = new ArrayList<Buss>();
	
	public Stop(Location l) {
		Name = l.desc;
		StopID = l.locid;
		Direction = l.dir;
	}
	
	public String toString() {
		return Name + " " + StopID;
	}
}
