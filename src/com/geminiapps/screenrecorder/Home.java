package com.geminiapps.screenrecorder;

import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.geminiapps.screenrecoder.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Home extends Activity {
	Button startbtn;
	Button stopbtn;
	AutoCompleteTextView recordtime;
	AutoCompleteTextView filename;

	Process su = null;
	DataOutputStream outputStream;

	String pid;
	private AdView adView;
	/* Your ad unit id. Replace with your actual ad unit id. */
	private static final String AD_UNIT_ID = "ca-app-pub-5800761622766190/7953199661";

	boolean isrooted = false;
	boolean startedrecording = false;
	Intent service_intent = new Intent("com.geminiapps.screenrecorder.service");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		startbtn = (Button) findViewById(R.id.startbtn);
		stopbtn = (Button) findViewById(R.id.stopbtn);
		recordtime = (AutoCompleteTextView) findViewById(R.id.recordtime);
		filename = (AutoCompleteTextView) findViewById(R.id.filename);

		EasyTracker.getInstance(this).activityStart(this);

		// Create an ad.
		adView = new AdView(this);
		adView.setAdSize(AdSize.BANNER);
		adView.setAdUnitId(AD_UNIT_ID);

		// Add the AdView to the view hierarchy. The view will have no size
		// until the ad is loaded.
		LinearLayout layout = (LinearLayout) findViewById(R.id.ad);
		layout.addView(adView);

		// Create an ad request. Check logcat output for the hashed device ID to
		// get test ads on a physical device.
		AdRequest adRequest = new AdRequest.Builder().build();

		// Start loading the ad in the background.
		adView.loadAd(adRequest);

		if (!new Root().isDeviceRooted())
			Toast.makeText(
					getApplicationContext(),
					"Sorry screen recorder needs root access, before you use the app, please root your phone.",
					Toast.LENGTH_LONG).show();
		else
			isrooted = true;

		File wallpaperDirectory = new File("/sdcard/ScreenRecord/");
		// have the object build the directory structure, if needed.
		wallpaperDirectory.mkdirs();

		startbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!isrooted)
					Toast.makeText(
							getApplicationContext(),
							"Sorry screen recorder needs root access, before you use the app, please root your phone.",
							Toast.LENGTH_LONG).show();
				if (startedrecording) {
					Toast.makeText(getApplicationContext(),
							"Record is already running.", Toast.LENGTH_LONG)
							.show();
					return;
				}
				// create a File object for the parent directory
				SimpleDateFormat s = new SimpleDateFormat("_yyyyMMdd_HHmmss");
				String date = s.format(new Date());
				final String command = "screenrecord --time-limit "
						+ recordtime.getText().toString()
						+ " /sdcard/ScreenRecord/"
						+ filename.getText().toString().replace(" ", "_")
						+ date + ".mp4 &\n";

				final String[] cmd = new String[2];
				cmd[0] = command;
				cmd[1] = "exit\n";

				System.out.println("start pressed, command:" + command);
				Thread thread = new Thread() {
					@Override
					public void run() {
						if (new ExecShell().executeSuCommand(cmd)) {
							System.out.println("record start");
							service_intent.putExtra("recordtime", recordtime
									.getText().toString());

							startService(service_intent);

							startedrecording = true;
							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(getApplicationContext(),
											"Record started", Toast.LENGTH_LONG)
											.show();
								}
							});
						} else {
							System.out.println("record not started");
							startedrecording = false;
							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(
											getApplicationContext(),
											"Access root permission denied, recording failed",
											Toast.LENGTH_LONG).show();
								}
							});
						}
					}
				};
				thread.start();
			}

		});

		stopbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("stop pressed");
				if (startedrecording) {
					Toast.makeText(getApplicationContext(),
							"Recording in the background", Toast.LENGTH_LONG)
							.show();
					finish();
				} else {
					Toast.makeText(getApplicationContext(),
							"Record not running", Toast.LENGTH_LONG).show();
					finish();
				}
			}

		});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
	}

	@Override
	public void onPause() {
		if (adView != null) {
			adView.pause();
		}
		super.onPause();
	}

	/** Called before the activity is destroyed. */
	@Override
	public void onDestroy() {
		// Destroy the AdView.
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
		EasyTracker.getInstance(this).activityStop(this);
		System.out.println("Home activity destroyed");
	}
	
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
        	System.out.println("back pressed");
			if (startedrecording) {
				Toast.makeText(getApplicationContext(),
						"Recording in the background", Toast.LENGTH_LONG)
						.show();
				finish();
			} else {
				Toast.makeText(getApplicationContext(),
						"Record not running", Toast.LENGTH_LONG).show();
				finish();
			}
        }
        return super.dispatchKeyEvent(event);
    }

}
