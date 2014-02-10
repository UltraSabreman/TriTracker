package com.example.tritracker;

import com.example.tritracker.json.DetourJSONResult.ResultSet;

import java.util.ArrayList;


public class Alert {
	public String Discription = "";
	public ArrayList<Integer> AffectedLines = new ArrayList<Integer>();

	public Alert(ResultSet.Detour d) {
		Discription = d.desc;
		for (ResultSet.Detour.Route r : d.route)
			AffectedLines.add(Integer.valueOf(r.route));
	}

	public boolean affectsStop(Stop s) {
		if (s == null || s.Busses == null) return false;
		for (Buss b : s.Busses)
			for (Integer i : AffectedLines)
				if (i.intValue() == b.Route)
					return true;
		return false;
	}
}
