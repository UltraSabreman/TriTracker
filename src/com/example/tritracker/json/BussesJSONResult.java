package com.example.tritracker.json;

public class BussesJSONResult {
	public ResultSet resultSet;

	public class ResultSet {
		public errorMessage errorMessage;
		public Vehicle[] vehicle;
		public long queryTime;

		public class Vehicle {
			public String signMessageLong;
			public String signMessage;
			public int routeNumber;
			public double longitude;
			public double latitude;
			public int bearing;
			public int vehicleID;
			public int blockID;
		}
	}

	public class errorMessage {
		public String content = "";
	}
}
