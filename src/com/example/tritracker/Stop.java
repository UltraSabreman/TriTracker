package com.example.tritracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.tritracker.json.JSONResult;
import com.google.gson.annotations.Expose;
//make me more specific
//import android.content.

public class Stop implements Parcelable {
	@Expose	public String Name = "Invalid Street";
	@Expose	public int StopID = -1;
	@Expose	public String Direction = "Up Up and Away!";
	@Expose	public Date LastAccesed = null;
	
	@Expose	public boolean inHistory = false;
	@Expose	public boolean inFavorites = false;
	
	@Expose	public ArrayList<Buss> Busses = new ArrayList<Buss>();
	
	public ArrayList<Alert> Alerts = new ArrayList<Alert>();

	public Stop(JSONResult.ResultSet.Location l) {
		Name = l.desc;
		StopID = l.locid;
		Direction = l.dir;
	}

	public Stop(int id) {
		StopID = id;
	}

	public void resetAlerts() {
		if (Alerts != null) {
			Alerts.clear();
			Alerts = new ArrayList<Alert>();
		}
	}
	
	public boolean hasNotifications() {
		if (Busses != null && Busses.size() != 0)
			for (Buss b : Busses)
				if (b.notification != null && b.notification.IsSet)
					return true;
		return false;
	}

	public void Update(Stop s, boolean shouldUpdateDate) {
		Name = new String(s.Name);
		StopID = s.StopID;
		Direction = new String(s.Direction);
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

	public static class Alert {
		public String Discription = "";
		public int AffectedLine = -1;

		public Alert(String d, int a) {
			Discription = d;
			AffectedLine = a;
		}
	}

	@Override
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
		
		out.writeList(Busses);
		out.writeList(Alerts);
	}
	
	public Stop(Parcel in) {
		Name = in.readString();
		StopID = in.readInt();
		Direction = in.readString();
		LastAccesed = new Date(in.readLong());
		inHistory = in.readInt() == 1 ? true : false;
		inFavorites = in.readInt() == 1 ? true : false;
		
		Busses = new ArrayList<Buss>();
		in.readList(Busses, Buss.class.getClassLoader());

		
		Alerts = new ArrayList<Alert>();
		in.readList(Alerts, Alert.class.getClassLoader());
	}
	
	public static final Parcelable.Creator<Stop> CREATOR = new Parcelable.Creator<Stop>() {
		public Stop createFromParcel(Parcel in) {
		    return new Stop(in);
		}
		
		public Stop[] newArray(int size) {
		    return new Stop[size];
		}
	};
}
