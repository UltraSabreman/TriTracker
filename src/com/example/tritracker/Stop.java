package com.example.tritracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.example.tritracker.json.JSONResult;
import com.google.gson.annotations.Expose;
//make me more specific
//import android.content.

public class Stop {
	@Expose
	public String Name = "Invalid Street";
	@Expose
	public int StopID = -1;
	@Expose
	public String Direction = "Up Up and Away!";
	@Expose
	public Date LastAccesed = null;

	@Expose
	public ArrayList<Buss> Busses = new ArrayList<Buss>();
	public ArrayList<Alert> Alerts = new ArrayList<Alert>();

	public Stop(JSONResult.ResultSet.Location l) {
		Name = l.desc;
		StopID = l.locid;
		Direction = l.dir;
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
}
