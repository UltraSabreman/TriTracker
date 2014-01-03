package com.example.tritracker;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

//Oh my god I know this is absolutely terrible and stupid
//but i'm just learning java + the android API and I want to make
//this as painless as possible. 

//I promise to come back and repent for my sins once i finish this/
//get more experience.
public class GlobalData {
	public static ArrayList<Stop> Favorites = new ArrayList<Stop>();
	public static ArrayList<Stop> History = new ArrayList<Stop>();
	public static int FavOrder = 0;
	public static int HistOrder = 0;
	public static int StopOrder = 0;
	public static int RefreshDelay = 5;
	public static Stop CurrentStop = null;
	public static int Orientation;
	public static ArrayList<Stop> Undos = new ArrayList<Stop>(); // TODO
		
	public static StopArrayAdaptor favAdaptor;
	public static StopArrayAdaptor histAdaptor;
	public static BussArrayAdaptor bussAdaptor;

	public static JsonWrapper getJsonWrap() {
		return new JsonWrapper(Favorites, History, FavOrder, HistOrder, StopOrder, RefreshDelay);
	}

	public static class JsonWrapper {
		@Expose public ArrayList<Stop> Favorites = new ArrayList<Stop>();
		@Expose public ArrayList<Stop> History = new ArrayList<Stop>();
		@Expose public int FavOrder = 0;
		@Expose public int HistOrder = 0;
		@Expose public int StopOrder = 0;
		@Expose public int RefreshDelay = 0;

		public JsonWrapper(ArrayList<Stop> fav, ArrayList<Stop> hist, int f, int h, int s, int r) {
			Favorites = fav;
			History = hist;
			FavOrder = f;
			HistOrder = h;
			StopOrder = s;
			RefreshDelay = r;
		}
	}

}
