package com.example.tritracker;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.tritracker.json.Location;
//make me more specific
//import android.content.

public class Stop implements Parcelable {
	public String Name = "Invalid Street";
	public int StopID = -1;
	public String Direction = "Up Up and Away!";

	public ArrayList<Buss> Busses = new ArrayList<Buss>();

	public Stop(Location l) {
		Name = l.desc;
		StopID = l.locid;
		Direction = l.dir;
	}

	public void Update(Stop s) {
		Name = new String(s.Name);
		StopID = s.StopID;
		Direction = new String(s.Direction);
		Busses.clear();
		for (Buss b: s.Busses)
			Busses.add(new Buss(b));			
	}

	public String toString() {
		return Name + " " + StopID;
	}

	// 99.9% of the time you can just ignore this
	public int describeContents() {
		return 0;
	}

	// write your object's data to the passed-in Parcel
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(Name);
		out.writeInt(StopID);
		out.writeString(Direction);
	}

	// this is used to regenerate your object. All Parcelables must have a
	// CREATOR that implements these two methods
	public static final Parcelable.Creator<Stop> CREATOR = new Parcelable.Creator<Stop>() {
		public Stop createFromParcel(Parcel in) {
			return new Stop(in);
		}

		public Stop[] newArray(int size) {
			return new Stop[size];
		}
	};

	// example constructor that takes a Parcel and gives you an object populated
	// with it's values
	private Stop(Parcel in) {
		Name = in.readString();
		StopID = in.readInt();
		Direction = in.readString();
	}
}
