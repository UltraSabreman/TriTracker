package com.example.tritracker.json;

import java.util.HashMap;
import java.util.Map;

public class Arrival {
	public boolean detour;
	public String status;
	public String scheduled;
	public String shortSign;
	public String estimated;
	public int route;
	public String fullSign;
	
	public Map<String, Object> otherProperties = new HashMap<String, Object>();
}
