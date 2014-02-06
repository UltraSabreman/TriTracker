package com.example.tritracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.example.tritracker.activities.MainActivity;
import com.example.tritracker.activities.StopListFragment;

import java.util.Date;

public class NotificationHandler {
	private Context notContext;
	private Timer timer;
	private NotificationManager not;
    private Buss trackedBuss;
    private int arrival;
	private Stop trackedStop;
	private int timeToWait;

	public boolean IsSet = false;

	public int getTime() {
		return timeToWait;
	}

	public NotificationHandler(final Context c, Intent i, Stop s, Buss b, int pos, int time) {
		timer = new Timer(1);
		set(c, i, s, b, pos, time);
	}

	public void set(final Context c, Intent i, Stop s, Buss b, int pos, int time) {
		trackedBuss = b;
		trackedStop = s;
		notContext = c;
        arrival = pos;
		timeToWait = time;

		timer.addCallBack("main", new Timer.onUpdate() {
			public void run() {
				Date est = null;
                //TODO make this work with more
				if (trackedBuss.times.get(arrival).Status.compareTo("estimated") == 0)
					est = new Date(trackedBuss.times.get(arrival).EstimatedTime.getTime() - new Date().getTime());
				else
					est = new Date(trackedBuss.times.get(arrival).ScheduledTime.getTime() - new Date().getTime());
				
				long test = est.getTime() / 1000 / 60; //Util.getTimeFromDate(est, TimeType.Minute);
				if (test <= timeToWait) {
					doNotification();
					timer.stopTimer();
					timer.removeCallBack("main");
					IsSet = false;
				}
			}
		});
		
		timer.restartTimer();
		IsSet = true;
	}

	public void cancelNotification() {
		timer.stopTimer();
		IsSet = false;
		if (not != null)
			not.cancel(0);
	}

	public void editNotification(int time) {
		timer.stopTimer();
		timeToWait = time;
		timer.restartTimer();
	}

	public Buss getBuss() {
		return trackedBuss;
	}
	
	public Stop	getStop() {
		return trackedStop;
	}

	private void doNotification() {
		IsSet = false;
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				notContext)
				// .setDeleteIntent(PendingIntent.getActivity(notContext, 0, notIntent, 0))
				.setSmallIcon(R.drawable.ic_buss_white)
				.setContentTitle(trackedBuss.SignShort)
				.setContentText(trackedStop.Name)
				.setTicker("Buss Arrival Alert")
                //TODO make this work for more
				.setWhen(trackedBuss.times.get(arrival).EstimatedTime != null ?
                trackedBuss.times.get(arrival).EstimatedTime.getTime() : trackedBuss.times.get(arrival).ScheduledTime.getTime())
				.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
				.setAutoCancel(true).setLights(0xffFF8800, 1500, 1000);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(notContext, StopListFragment.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(notContext);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		not = (NotificationManager) notContext.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		not.notify(0, mBuilder.build());
	}

}
