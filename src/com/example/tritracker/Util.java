package com.example.tritracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
}
