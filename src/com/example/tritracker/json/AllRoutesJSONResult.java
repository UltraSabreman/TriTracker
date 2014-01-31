package com.example.tritracker.json;

public class AllRoutesJSONResult {
	public ResultSet resultSet;

	public class ResultSet {
		public Route[] route;
		public String queryTime;
		public errorMessage errorMessage;

		public class Route {
			public boolean detour;
			public String desc;
			public Dir[] dir;
			public int route;
			public String type = "";
			
			public class Dir {
				public Stop[] stop;
				public String desc;
				public int dir;

				public class Stop {
					public String desc;
					public int locid;
					public double lng;
					public double lat;
				}
			}
		}
		
		public class errorMessage {
			public String content = "";
		}
	}
}
