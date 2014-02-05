package com.example.tritracker;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.example.tritracker.activities.MainActivity;
import com.example.tritracker.activities.StopListFragment;

public class NotificationHandler {
	private Context notContext;
	private Timer timer;
	private NotificationManager not;
	public Buss trackedBuss;
	private Stop trackedStop;
	private int timeToWait;

	public boolean IsSet = false;

	public int getTime() {
		return timeToWait;
	}

	public NotificationHandler(final Context c, Intent i, Stop s, Buss b, int time) {
		timer = new Timer(1);
		set(c, i, s, b, time);
	}

	public void set(final Context c, Intent i, Stop s, Buss b, int time) {
		trackedBuss = b;
		trackedStop = s;
		notContext = c;
		timeToWait = time;

		timer.addCallBack("main", new Timer.onUpdate() {
			public void run() {
				Date est = null;
				if (trackedBuss.Status.compareTo("estimated") == 0)
					est = new Date(trackedBuss.EstimatedTime.getTime() - new Date().getTime());
				else
					est = new Date(trackedBuss.ScheduledTime.getTime() - new Date().getTime());
				
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
				.setWhen(trackedBuss.EstimatedTime != null ? trackedBuss.EstimatedTime.getTime() : trackedBuss.ScheduledTime.getTime())
				.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
				.setAutoCancel(true).setLights(0xffFF8800, 1500, 1000);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(notContext, StopListFragment.class);

		// The stack builder object will contain an artificial back stack for
		// the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
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
