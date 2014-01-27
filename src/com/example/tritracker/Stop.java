package com.example.tritracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.example.tritracker.json.AllRoutesJSONResult;
import com.example.tritracker.json.ArrivalJSONResult;
import com.example.tritracker.json.MapJSONResult;
//make me more specific
//import android.content.

public class Stop  {
	public String Name = "Invalid Street";
	public int StopID = -1;
	public String Direction = "Up Up and Away!";
	public Date LastAccesed = null;
	
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
	}
	
	public Stop(MapJSONResult.ResultSet.Location l) {
		Name = l.desc;
		StopID = l.locid;
		Direction = l.dir;
		Latitude = l.lat;
		Longitude = l.lng;
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
	}

	public Stop(int id) {
		StopID = id;
	}

	/*public String getService() {
		if (Busses != null) {
			String s = "";
			for (Buss b : Busses) {
				s += ro + ", ";
			}
			
			if (s.compareTo("") != 0) {
				s = s.substring(0, s.length() - 2);
			}
			return s;
		}
		return "";
	}*/
		

	public void Update(Stop s, boolean shouldUpdateDate) {
		Name = new String(s.Name);
		StopID = s.StopID;
		Direction = new String(s.Direction);
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

			for (Iterator<Buss> it = Busses.iterator(); it.hasNext();)
				if (s.getBuss(it.next()) == null)
					it.remove();
		}
	}

	public Buss getBuss(Buss ib) {
		for (Buss b : Busses)
			if (b != null && b.compareTo(ib))
				return b;
		return null;
	}


	/*@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {	
		out.writeString(Name);
		out.writeInt(StopID);
		out.writeString(Direction);
		out.writeLong(LastAccesed.getTime());
		out.writeInt(inHistory ? 1 : 0);
		out.writeInt(inFavorites ? 1 : 0);
		out.writeDouble(Latitude);
		out.writeDouble(Longitude);
		
		out.writeList(Busses);
	}
	
	public Stop(Parcel in) {
		Name = in.readString();
		StopID = in.readInt();
		Direction = in.readString();
		LastAccesed = new Date(in.readLong());
		inHistory = in.readInt() == 1 ? true : false;
		inFavorites = in.readInt() == 1 ? true : false;
		Latitude = in.readDouble();
		Longitude = in.readDouble();
		
		Busses = new ArrayList<Buss>();
		in.readList(Busses, Buss.class.getClassLoader());		
	}
	
	public static final Parcelable.Creator<Stop> CREATOR = new Parcelable.Creator<Stop>() {
		public Stop createFromParcel(Parcel in) {
		    return new Stop(in);
		}
		
		public Stop[] newArray(int size) {
		    return new Stop[size];
		}
	};*/
}
