package com.example.tritracker;

import com.example.tritracker.json.ArrivalJSONResult;

import java.util.ArrayList;
import java.util.Date;

public class Buss {
	public int Route = -1;
	public boolean Detouring;
	public String SignShort;
	public String SignLong;
	public int BlockID = -1;

	public ArrayList<TimeBox> times = new ArrayList<TimeBox>();

	public class TimeBox {
		public String Status = "";
		public Date EstimatedTime = null;
		public Date ScheduledTime = null;

		public TimeBox(Date e, Date s, String st) {
			EstimatedTime = e;
			ScheduledTime = s;
			if (st != null)
				Status = st;
		}

	}

	public Buss(ArrivalJSONResult.ResultSet.Arrival a) {
		Route = a.route;
		Detouring = a.detour;

		SignShort = a.shortSign;
		SignLong = a.fullSign;

		BlockID = a.block;

		times.add(new TimeBox(Util.dateFromString(a.estimated), Util.dateFromString(a.scheduled), a.status));
	}

	public void AddTime(ArrivalJSONResult.ResultSet.Arrival a) {
		times.add(new TimeBox(Util.dateFromString(a.estimated), Util.dateFromString(a.scheduled), a.status));
	}

	public Buss(Buss b) {
		update(b);
	}

	public boolean compareTo(Buss b) {
		if (b == null)
			return false;
		if (SignLong.compareTo(b.SignLong) == 0)
			return true;
		return false;
	}

	public void update(Buss b) {
		Route = b.Route;
		Detouring = b.Detouring;
		SignShort = new String(b.SignShort);
		SignLong = new String(b.SignLong);
		BlockID = b.BlockID;

		if (b.times != null) {
			times.clear();
			times.addAll(b.times);
		}
	}
}
