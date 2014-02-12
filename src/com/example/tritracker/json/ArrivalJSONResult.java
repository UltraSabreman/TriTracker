package com.example.tritracker.json;

public class ArrivalJSONResult {
	public ResultSet resultSet;

	public class ResultSet {
		public Location[] location;
		public Arrival[] arrival;
		public String queryTime;
		public errorMessage errorMessage;

		public class Location {
			public String dir;
			public String desc;
			public int locid;
			public double lng;
			public double lat;
		}

		public class Arrival {
			public boolean detour;
			public String status;
			public String scheduled;
			public String shortSign;
			public String estimated;
			public int route;
			public int locid;
			public String fullSign;
			public int block;
		}

		public class errorMessage {
			public String content = "";
		}
	}
}
