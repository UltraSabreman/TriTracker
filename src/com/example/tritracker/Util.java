package com.example.tritracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

public class Util {
	public static Date dateFromString(String s){
		if (s == null) return null;
		try {
			s = s.replace("T","");
			return new SimpleDateFormat("yyyy-MM-ddHH:mm:ss.SSSZ", Locale.US).parse(s);
		}catch (ParseException e) {
			return null;
		}
	}
	
	public static Stack<Class<?>> parents = new Stack<Class<?>>();
}
