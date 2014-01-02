package com.example.tritracker;

import java.util.Date;
//import java.util.Vector;

import com.example.tritracker.json.Arrival;

public class Buss {
	public int Route = -1;
	public boolean Detouring;
	public String Status;
	public String SignShort;
	public String SignLong;
	public Date EstimatedTime;
	public Date ScheduledTime;

	public Buss(Arrival a) {
		Route = a.route;
		Detouring = a.detour;
		Status = a.status;
		SignShort = a.shortSign;
		SignLong = a.fullSign;

		EstimatedTime = Util.dateFromString(a.estimated);
		ScheduledTime = Util.dateFromString(a.scheduled);

	}

	public Buss(Buss b) {
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
