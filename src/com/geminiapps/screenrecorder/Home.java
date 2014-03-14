package com.geminiapps.screenrecorder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import com.geminiapps.screenrecoder.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.os.Bundle;
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

		startbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final String command = "screenrecord --time-limit "
						+ recordtime.getText().toString() + " /sdcard/"
						+ filename.getText().toString() + ".mp4\n";

				System.out.println("start pressed, command:" + command);
				Thread thread = new Thread() {
					@Override
					public void run() {
						try {
							su = Runtime.getRuntime().exec("su");
							outputStream = new DataOutputStream(su
									.getOutputStream());
							BufferedReader in = new BufferedReader(
									new InputStreamReader(su.getInputStream()));
							outputStream.writeBytes(command);
							outputStream.flush();
							outputStream.writeBytes("exit\n");
							outputStream.flush();

							String s = "";
							s = in.readLine();
							while (s != null) {
								System.out.println(s);
								s = in.readLine();
							}
							// pid=in.readLine();
							su.waitFor();
							if (su == null)
								runOnUiThread(new Runnable() {
									public void run() {
										Toast.makeText(
												getApplicationContext(),
												"Access root permission denied, recording failed",
												Toast.LENGTH_LONG).show();
									}

								});
							else
								runOnUiThread(new Runnable() {
									public void run() {
										Toast.makeText(getApplicationContext(),
												"Record started",
												Toast.LENGTH_LONG).show();
									}

								});
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
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
				if (su != null) {
					su.destroy();
					su = null;
					Toast.makeText(getApplicationContext(),
							"Recording in the background", Toast.LENGTH_LONG)
							.show();
					finish();
				} else {
					Toast.makeText(getApplicationContext(),
							"Record not running", Toast.LENGTH_LONG).show();
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
}
