package com.example.tritracker.json;

public class ResultDetour {
	public ResultSet resultSet;
	
	public class ResultSet {
		public Detour[] detour;
		public String queryTime;
		public errorMessage errorMessage;

		public class Detour {
			public String desc = "";
			public Route[] route;

			public class Route {
				public boolean detour;
				public String desc = "";
				public int route;
				public String type = "";
			}
		}
		
		public class errorMessage {
			public String content = "";
		}
	}
}
