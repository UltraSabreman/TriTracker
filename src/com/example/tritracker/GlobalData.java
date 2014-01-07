package com.example.tritracker;

import java.util.ArrayList;


import com.example.tritracker.ArrayAdaptors.BussArrayAdaptor;
import com.example.tritracker.ArrayAdaptors.StopArrayAdaptor;
import com.google.gson.annotations.Expose;

//Oh my god I know this is absolutely terrible and stupid
//but i'm just learning java + the android API and I want to make
//this as painless as possible. 

//I promise to come back and repent for my sins once i finish this/
//get more experience.
public class GlobalData {
	public static int FavOrder = 0;
	public static int HistOrder = 0;
	public static int StopOrder = 0;
	public static int RefreshDelay = 5;
	public static int Orientation;

	public static Stop CurrentStop = null;

	public static ArrayList<Stop> Favorites = new ArrayList<Stop>();
	public static ArrayList<Stop> History = new ArrayList<Stop>();

	public static ArrayList<Stop> FUndos = new ArrayList<Stop>();
	public static ArrayList<Stop> HUndos = new ArrayList<Stop>();

	public static StopArrayAdaptor favAdaptor;
	public static StopArrayAdaptor histAdaptor;
	public static BussArrayAdaptor bussAdaptor;

	public static JsonWrapper getJsonWrap() {
		return new JsonWrapper();
	}

	public static class JsonWrapper {
		@Expose
		public ArrayList<Stop> stops = new ArrayList<Stop>();
		@Expose
		public int FavOrder = 0;
		@Expose
		public int HistOrder = 0;
		@Expose
		public int StopOrder = 0;
		@Expose
		public int RefreshDelay = 0;
		
		private boolean hasStop(Stop s) {
			for (Stop st : stops)
				if (s.StopID == st.StopID)
					return true;
			return false;
		}

		public JsonWrapper() {
			FavOrder = GlobalData.FavOrder;
			HistOrder = GlobalData.HistOrder;
			StopOrder = GlobalData.StopOrder;
			RefreshDelay = GlobalData.RefreshDelay;
			
			
			for (Stop s : GlobalData.History) {		
				s.inHistory = true;
				if (Util.favHasStop(s))
					s.inFavorites = true;
				else
					s.inFavorites = false;
				stops.add(s);
			}
			
			for (Stop s : GlobalData.Favorites) {
				if (!hasStop(s)) {
					s.inHistory = false;
					s.inFavorites = true;
					stops.add(s);
				}
			}
		}
	}

}
