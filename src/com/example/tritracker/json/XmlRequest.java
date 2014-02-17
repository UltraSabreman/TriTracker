package com.example.tritracker.json;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("kml")
public class XmlRequest {
	@XStreamAlias("xmlns")
	@XStreamAsAttribute
	public String type;

	@XStreamAlias("Document")
	public document Document;

	public class document {
		@XStreamImplicit(itemFieldName="Placemark")
		public List<placemark> BusRoutes = new ArrayList<placemark>();

		public class placemark {
			@XStreamAlias("ExtendedData")
			public Edata RouteInfo;

			@XStreamAlias("MultiGeometry")
			public MulGeo RouteCoordinates;

			public Edata.Data getDataByName(String name) {
				for (Edata.Data d : RouteInfo.InfoList)
					if (d.Name.compareTo(name) == 0)
						return d;
				return null;
			}

			public class Edata {
				@XStreamImplicit(itemFieldName="Data")
				public List<Data> InfoList = new ArrayList<Data>();

				public class Data {
					@XStreamAlias("name")
					@XStreamAsAttribute
					public String Name;

					@XStreamAlias("value")
					public String Value;
				}
			}

			public class MulGeo {
				@XStreamImplicit(itemFieldName="LineString")
				public List<LineString> RouteSections = new ArrayList<LineString>();

				public class LineString {
					@XStreamAlias("coordinates")
					public String Coordinates;
				}
			}
		}
	}
}
