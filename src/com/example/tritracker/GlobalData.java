package com.example.tritracker;

import java.util.ArrayList;

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
	public static ArrayList<Stop> Undos = new ArrayList<Stop>(); // TODO
	public static Stop CurrentStop = null;
	public static int Orientation;

	public static JsonWrapper getJsonWrap() {
		return new JsonWrapper(Favorites, History, FavOrder, HistOrder, StopOrder);
	}

	public static class JsonWrapper {
		public ArrayList<Stop> Favorites = new ArrayList<Stop>();
		public ArrayList<Stop> History = new ArrayList<Stop>();
		public int FavOrder = 0;
		public int HistOrder = 0;
		public int StopOrder = 0;

		public JsonWrapper(ArrayList<Stop> fav, ArrayList<Stop> hist, int f, int h, int s) {
			Favorites = fav;
			History = hist;
			FavOrder = f;
			HistOrder = h;
			StopOrder = s;
		}
	}

}
