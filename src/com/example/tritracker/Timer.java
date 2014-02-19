package com.example.tritracker;

import android.os.Handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Timer {
	private double interval = 0;
	private Handler timerHandler = new Handler();
	private Runnable timerRunnable = null;
	private Map<String, onUpdate> updateList = new HashMap<String, onUpdate>();
	private boolean kill = false;

	public Timer(double d) {
		this.interval = d;
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
						Map.Entry<String, onUpdate> pairs = (Map.Entry<String, onUpdate>) it.next();
						if (pairs != null && pairs.getValue() != null)
                            ((onUpdate) pairs.getValue()).run();
					}
				}

				if (interval > 0 && !kill) {
					Lock lock = new ReentrantLock();

					try {
						lock.tryLock(100, TimeUnit.MILLISECONDS);
						timerHandler.postDelayed(this, (int) (interval * 1000));
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						lock.unlock();
					}
				}
				if (kill)
					kill = false;
			}
		};

		if (interval > 0)
			timerHandler.postDelayed(timerRunnable, (int) (interval * 1000));
	}

	public void stopTimer() {
		if (timerRunnable != null)
			timerHandler.removeCallbacks(timerRunnable);
		kill = true;
		timerRunnable = null;
	}

	public interface onUpdate {
        public void run();
	}

}
