package com.example.tritracker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Handler;

public class Timer {
	private int interval = 0;
	private Handler timerHandler = new Handler();
	private Runnable timerRunnable = null;
	private Map<String, onUpdate> updateList = new HashMap<String, onUpdate>();
	
	public Timer(int interval) {
		this.interval = interval;
	}
	
	public void updateDelay(int newInterval) {
		interval = newInterval;
		
		restartTimer();
	}
	
	public void addCallBack(String key, onUpdate func) {
		if (!updateList.containsKey(key))
			updateList.put(key, func);
	}
	
	public void removeCallBack(String key) {
		if (updateList.containsKey(key))
			updateList.remove(key);
	}
	
	public void restartTimer() {
		if (timerRunnable != null)
			timerHandler.removeCallbacks(timerRunnable);

		timerRunnable = new Runnable() {
			@Override
			public void run() {
				if (updateList != null && updateList.size() != 0) {
					Iterator<Entry<String, onUpdate>> it = updateList.entrySet().iterator();
				    while (it.hasNext()) {
				        Map.Entry<String, onUpdate> pairs = (Map.Entry<String, onUpdate>)it.next();
				        if(pairs != null && pairs.getValue() != null)
				        	((onUpdate)pairs.getValue()).run();
				    }
				}
				
				if (interval> 0)
					timerHandler.postDelayed(this,
							(int) (interval * 1000));
			}
		};

		if (interval > 0)
			timerHandler.post(timerRunnable);
	}

	public void stopTimer() {
		if (timerRunnable != null)
			timerHandler.removeCallbacks(timerRunnable);
	}
	
	public interface onUpdate {
		public void run();
	}

}
