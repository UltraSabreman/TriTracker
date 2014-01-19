package com.example.tritracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.example.tritracker.Util.ListType;
import com.example.tritracker.activities.MainService;

public class Sorter <T> {
	public int sortOrder = 0;
	//Sorting//
    private final Class<T> type;
    private MainService theService;

    public Sorter(Class<T> type, MainService service) {
         this.type = type;
         this.theService = service;
    }
    
    public void sort(final ListType listtype, final ArrayList<T> listToSort, final Timer.onUpdate callback) {
    	sortOrder = theService.getSort(listtype);
    	sortList(listToSort, listtype);
    }
    
	public void sortUI(Activity a, final ListType listtype, final ArrayList<T> listToSort, final Timer.onUpdate callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(a);

		builder.setTitle("Sort By");
		String[] list = new String[] { "Stop name", "Stop ID", "Last Accesed" };
		if (type == Buss.class)
			list = new String[] { "Route Name", "Route Number", "Arrival Time" };

		sortOrder = theService.getSort(listtype);
		builder.setSingleChoiceItems(list, theService.getSort(listtype),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sortOrder = which;
					}
				});
		
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				theService.setSort(listtype, sortOrder);
				sortList(listToSort, listtype);
				if (callback != null)
					callback.run();
			}
		});

		builder.create().show();
	}
	
	@SuppressWarnings("unchecked")
	public boolean sortList(ArrayList<T> list, ListType listtype) {
		if (list == null)
			return false;
		if (listtype == ListType.Favorites) {
			Collections.sort((ArrayList<Stop>)list, new StopSorter(sortOrder));
			if (sortOrder == 2)	Collections.reverse(list);
				return true;
		} else if (listtype == ListType.History) { // History
			Collections.sort((ArrayList<Stop>)list, new StopSorter(sortOrder));
			if (sortOrder == 2) Collections.reverse(list);
				return true;
		} else if (listtype == ListType.Busses) { // Buss
			Collections.sort((ArrayList<Buss>)list, new BussSorter(sortOrder));
			return true;
		}
		return false;
	}
	
	private static class StopSorter implements Comparator<Stop> {
		private int compareType = 0;

		public StopSorter(int t) {
			compareType = t;
			// 0 == By Name
			// 1 == By ID
			// 2 == By Acces Date
		}

		@Override
		public int compare(Stop s, Stop s2) {
			if (s == null || s2 == null)
				return 0;
			if (compareType == 0)
				return s.Name.compareTo(s2.Name);
			if (compareType == 1)
				return (s.StopID < s2.StopID ? -1 : (s.StopID > s2.StopID ? 1 : 0));
			else {
				if (s.LastAccesed != null && s2.LastAccesed != null)
					return s.LastAccesed.compareTo(s2.LastAccesed);
				else 
					return 0;
			}
		}
	}

	private static class BussSorter implements Comparator<Buss> {
		private int compareType = 0;

		public BussSorter(int t) {
			compareType = t;
			// 0 == By Name
			// 1 == By Line
			// 2 == By Arrival Time
		}

		@Override
		public int compare(Buss o1, Buss o2) {
			if (compareType == 0)
				/*if (GlobalData.Orientation == 2)
					return o1.SignLong.compareTo(o2.SignLong);
				else*/
				//TODO Sorting
					return o1.SignShort.compareTo(o2.SignShort);
			if (compareType == 1)
				return (o1.Route < o2.Route ? -1
						: (o1.Route > o2.Route ? 1 : 0));
			else if (o1.EstimatedTime != null && o2.EstimatedTime != null)
				return o1.EstimatedTime.compareTo(o2.EstimatedTime); // fix me
			else
				return o1.ScheduledTime.compareTo(o2.ScheduledTime); // fix me
		}
	}
}
