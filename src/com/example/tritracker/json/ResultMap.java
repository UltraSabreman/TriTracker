package com.example.tritracker.json;

public class ResultMap {
	public ResultSet resultSet;

	public class ResultSet {
		public errorMessage errorMessage;
		public Location[] location;
		public String queryTime;

		public class Location {
			public String desc;
			public int locid;
			public String dir;	
			public Route[] route;
			public double lng;
			public double lat;
			
			public class Route {
				public String desc;
				public int route;
				public String type;
			}
		}
	}
	
	public class errorMessage {
		public String content = "";
	}
}
