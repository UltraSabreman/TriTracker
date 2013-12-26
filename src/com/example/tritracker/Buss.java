package com.example.tritracker;

import java.util.Date;
import java.util.Vector;

public class Buss {
	public int RouteID = -1;
	public Vector<Date> Arrivals = null;
	
	public Buss() {
		Arrivals = new Vector<Date>();//{new Date(), new Date(123)};
		Arrivals.add(new Date(99999));
		Arrivals.add(new Date());
	}
}
