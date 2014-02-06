package com.example.tritracker;

import com.example.tritracker.json.ArrivalJSONResult;

import java.util.ArrayList;
import java.util.Date;

public class Buss {
	public int Route = -1;
	public boolean Detouring;
	public String SignShort;
	public String SignLong;

    public ArrayList<String> Stats = new ArrayList<String>();
	public ArrayList<Date> EstimatedTimes = new ArrayList<Date>();
	public ArrayList<Date> ScheduledTimes = new ArrayList<Date>();

	public Buss(ArrivalJSONResult.ResultSet.Arrival a) {
		Route = a.route;
		Detouring = a.detour;
		Stats.add(a.status);
		SignShort = a.shortSign;
		SignLong = a.fullSign;

        EstimatedTimes.add(Util.dateFromString(a.estimated));
		ScheduledTimes.add(Util.dateFromString(a.scheduled));

	}

    public void AddTime(ArrivalJSONResult.ResultSet.Arrival a) {
        Stats.add(a.status);
        EstimatedTimes.add(Util.dateFromString(a.estimated));
        ScheduledTimes.add(Util.dateFromString(a.scheduled));
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
        Stats.clear();
        Stats.addAll(b.Stats);
		SignShort = new String(b.SignShort);
		SignLong = new String(b.SignLong);

		if (b.EstimatedTimes != null) {
            EstimatedTimes.clear();
            EstimatedTimes.addAll(b.EstimatedTimes);
        }
        if (b.ScheduledTimes != null){
            EstimatedTimes.clear();
            EstimatedTimes.addAll(b.EstimatedTimes);
        }
		
	}
}
