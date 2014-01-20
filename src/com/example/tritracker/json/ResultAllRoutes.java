package com.example.tritracker.json;

public class ResultAllRoutes {
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
					public long lng;
					public long lat;
				}
			}
		}
		
		public class errorMessage {
			public String content = "";
		}
	}
}
