package com.example.tritracker;
import java.util.*; //make me more specific
//import android.content.

public class Stop  {
	private StopData info = null;
	
	public String GetStopLocation() { return this.info.Location; }
	public int GetStopID() { return this.info.StopID; }
	public Vector<Buss> GetBussLines() { return this.info.BussLines; }
	
	public Stop(StopData s) {
		info = s; //no need to deep copy.
	}
	
	public void SetStopData(StopData s) {
		if (s != null)
			info = s;
	}
	
	public String toString() {
		return info.Location + "     " + info.StopID;
	}
}
