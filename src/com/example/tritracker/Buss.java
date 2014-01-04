package com.example.tritracker;

import java.util.Date;

import com.example.tritracker.json.JSONResult;
import com.google.gson.annotations.Expose;
//import java.util.Vector;

public class Buss {
	@Expose public int Route = -1;
	@Expose public boolean Detouring;
	@Expose public String Status;
	@Expose public String SignShort;
	@Expose public String SignLong;
	@Expose public Date EstimatedTime;
	@Expose public Date ScheduledTime;
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
	
	public void setNotification(NotificationHandler n){
		notification = n;
	}
	
	public boolean compareTo(Buss b) {
		if (b == null) return false;
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
}
