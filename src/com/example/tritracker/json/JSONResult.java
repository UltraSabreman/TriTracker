package com.example.tritracker.json;

import java.util.HashMap;
import java.util.Map;

public class JSONResult {
	public ResultSet resultSet;
	public String queryTime;

	public class ResultSet {
		public errorMessage errorMessage;
		public String queryTime;
		public Location[] location;
		public Arrival[] arrival;
		public Detour[] detour;

		public class Detour {
			public String desc = "";
			public Route[] route;

			public class Route {
				public boolean detour;
				public String route = "";
				public String type = "";
			}
		}

		public class errorMessage {
			public String content = "";

		}

		public class Location {
			public String dir;
			public String desc;
			public int locid;
		}

		public class Arrival {
			public boolean detour;
			public String status;
			public String scheduled;
			public String shortSign;
			public String estimated;
			public int route;
			public String fullSign;
			public BlockPos blockPosition;
			public int locid;

			public Map<String, Object> otherProperties = new HashMap<String, Object>();

			public class BlockPos {
				public Trip[] trip;

				public class Trip {
					public String tripNum;
				}
			}
		}
	}
}
