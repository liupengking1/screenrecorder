package com.geminiapps.screenrecorder;

import java.util.Timer;
import java.util.TimerTask;

import com.geminiapps.screenrecoder.R;
import com.geminiapps.screenrecoder.R.string;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class BackendService extends Service {
	public class MyBinder extends Binder {
		public BackendService getService() {
			return BackendService.this;
		}
	}

	private MyBinder binder = new MyBinder();
	private Timer timer;
	int recordtime = 0;
	int count = 0;
	ToastHandler mToastHandler;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		System.out.println("on bind");
		return binder;
	}

	private void Notification(String msg) {
		Notification notification = new NotificationCompat.Builder(this)
				// .setContentIntent(contentIntent)
				.setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true)
				.setContentTitle(getResources().getString(R.string.app_name))
				.setContentText(msg).getNotification();
		startForeground(34546, notification);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Bundle extras = intent.getExtras();
		mToastHandler = new ToastHandler(getApplicationContext());
		String temp = null;
		if (extras != null) {
			temp = extras.getString("recordtime");
		}
		if (temp != null) {
			recordtime = Integer.parseInt(temp);
			count = recordtime;
		}
		System.out.println("recordtime:" + recordtime);
		startTimer();
	}

	@Override
	public void onCreate() {

		System.out.println("Service Started");

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		timercancel();
		System.out.println("service ended");

	}

	private class task extends TimerTask {
		@Override
		public void run() {
			if (count > 0) {
				count--;
			} else {
				if (count == 0)
					mToastHandler.showToast(
							getResources().getString(
									string.toast_record_complete),
							Toast.LENGTH_LONG);
				count--;
				BackendService.this.stopSelf();
			}
			String msg = getResources().getString(R.string.notification_text)
					+ count + "/" + recordtime;
			Notification(msg);
		}
	}

	private void startTimer() {
		timer = new Timer();
		timer.schedule(new task(), 0, 1000);
	}

	public void timercancel() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
	}

}
