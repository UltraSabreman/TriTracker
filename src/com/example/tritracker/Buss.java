package com.example.tritracker;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.transition.Scene;

import com.example.tritracker.json.JSONResult;
import com.google.gson.annotations.Expose;

//import java.util.Vector;

public class Buss implements Parcelable {
	@Expose	public int Route = -1;
	@Expose	public boolean Detouring;
	@Expose	public String Status;
	@Expose	public String SignShort;
	@Expose	public String SignLong;
	@Expose	public Date EstimatedTime;
	@Expose	public Date ScheduledTime;
	public NotificationHandler notification = null;

	public Buss(JSONResult.ResultSet.Arrival a) {
		Route = a.route;
		Detouring = a.detour;
		Status = a.status;
		SignShort = a.shortSign;
		SignLong = a.fullSign;

		EstimatedTime = Util.dateFromString(a.estimated);
		ScheduledTime = Util.dateFromString(a.scheduled);

	}

	public Buss(Buss b) {
		update(b);
	}

	public void setNotification(NotificationHandler n) {
		notification = n;
	}

	public boolean compareTo(Buss b) {
		if (b == null)
			return false;
		if (ScheduledTime.compareTo(b.ScheduledTime) == 0)
			return true;
		return false;
	}

	public void update(Buss b) {
		Route = b.Route;
		Detouring = b.Detouring;
		Status = new String(b.Status);
		SignShort = new String(b.SignShort);
		SignLong = new String(b.SignLong);

		if (b.EstimatedTime != null)
			EstimatedTime = new Date(b.EstimatedTime.getTime());
		if (b.ScheduledTime != null)
			ScheduledTime = new Date(b.ScheduledTime.getTime());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(Route);
		out.writeInt(Detouring ? 1 : 0);
		out.writeString(Status);
		out.writeString(SignShort);
		out.writeString(SignLong);
		
		out.writeLong(EstimatedTime.getTime());
		out.writeLong(ScheduledTime.getTime());
	}
	
	public Buss(Parcel in) {
		Route = in.readInt();
		Detouring = in.readInt() == 1 ? true : false;
		Status = in.readString();
		SignShort = in.readString();
		SignLong = in.readString();
		EstimatedTime = new Date(in.readLong());
		ScheduledTime = new Date(in.readLong());
	}
	
	public static final Parcelable.Creator<Buss> CREATOR = new Parcelable.Creator<Buss>() {
		public Buss createFromParcel(Parcel in) {
		    return new Buss(in);
		}
		
		public Buss[] newArray(int size) {
		    return new Buss[size];
		}
	};
}
