package com.example.tritracker;

import java.util.Date;

import com.example.tritracker.json.Arrival;
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
	//@Expose public String TripNumber;
	public NotificationHandler notification = null;

	public Buss(Arrival a) {
		Route = a.route;
		Detouring = a.detour;
		Status = a.status;
		SignShort = a.shortSign;
		SignLong = a.fullSign;
		//TripNumber = a.blockPosition.trip[0].tripNum;

		EstimatedTime = Util.dateFromString(a.estimated);
		ScheduledTime = Util.dateFromString(a.scheduled);

	}

	public Buss(Buss b) {
		update(b);
	}
	
	public void setNotification(NotificationHandler n){
		notification = n;
	}
	
	public void update(Buss b) {
		Route = b.Route;
		Detouring = b.Detouring;
		Status = new String(b.Status);
		SignShort = new String(b.SignShort);
		SignLong = new String(b.SignLong);
		//TripNumber = new String(b.TripNumber);

		if (b.EstimatedTime != null)
			EstimatedTime = new Date(b.EstimatedTime.getTime());
		if (b.ScheduledTime != null)
			ScheduledTime = new Date(b.ScheduledTime.getTime());
	}
}
