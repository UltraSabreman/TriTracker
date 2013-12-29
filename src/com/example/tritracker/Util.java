package com.example.tritracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Util {
	public static Date dateFromString(String s){
		try {
			s = s.replace("T","");
			return new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSSZ").parse(s);
		}catch (ParseException e) {
			return null;
		}
	}
	
	public static Date subtractDates(Date d1, Date d2) {
		Calendar start = Calendar.getInstance();
		start.setTime(d1);
		Calendar end = Calendar.getInstance();
		end.setTime(d2);

		Integer[] elapsed = new Integer[6];
		Calendar clone = (Calendar) start.clone(); // Otherwise changes are been reflected.
		elapsed[0] = elapsed(clone, end, Calendar.YEAR);
		clone.add(Calendar.YEAR, elapsed[0]);
		elapsed[1] = elapsed(clone, end, Calendar.MONTH);
		clone.add(Calendar.MONTH, elapsed[1]);
		elapsed[2] = elapsed(clone, end, Calendar.DATE);
		clone.add(Calendar.DATE, elapsed[2]);
		elapsed[3] = (int) (end.getTimeInMillis() - clone.getTimeInMillis()) / 3600000;
		clone.add(Calendar.HOUR, elapsed[3]);
		elapsed[4] = (int) (end.getTimeInMillis() - clone.getTimeInMillis()) / 60000;
		clone.add(Calendar.MINUTE, elapsed[4]);
		elapsed[5] = (int) (end.getTimeInMillis() - clone.getTimeInMillis()) / 1000;
		
		long totalMill = 0;
		for (int i : elapsed)
			totalMill += i;
		
		return (new Date(totalMill));
	}
	
	private static int elapsed(Calendar before, Calendar after, int field) {
	    Calendar clone = (Calendar) before.clone(); // Otherwise changes are been reflected.
	    int elapsed = -1;
	    while (!clone.after(after)) {
	        clone.add(field, 1);
	        elapsed++;
	    }
	    return elapsed;
	}
}
