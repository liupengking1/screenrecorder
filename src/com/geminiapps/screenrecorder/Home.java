package com.geminiapps.screenrecorder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.geminiapps.screenrecoder.R;
import com.geminiapps.screenrecoder.R.string;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Home extends Activity {
	Button startbtn;
	Button stopbtn;
	Button rateme;
	AutoCompleteTextView recordtime;
	AutoCompleteTextView filename;
	CheckBox checkbox;

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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbar_custom, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_rateme:
			openRatingPage();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void openRatingPage() {
		Uri uri = Uri.parse("market://details?id="
				+ getApplicationContext().getPackageName());
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getApplicationContext(),
					"Couldn't launch the market", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);
		startbtn = (Button) findViewById(R.id.startbtn);
		stopbtn = (Button) findViewById(R.id.stopbtn);
		recordtime = (AutoCompleteTextView) findViewById(R.id.recordtime);
		filename = (AutoCompleteTextView) findViewById(R.id.filename);
		final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		checkbox = (CheckBox) findViewById(R.id.checkBox1);
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
			Toast.makeText(getApplicationContext(),
					getResources().getString(string.toast_not_rooted),
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
					Toast.makeText(getApplicationContext(),
							getResources().getString(string.toast_not_rooted),
							Toast.LENGTH_LONG).show();
				if (startedrecording) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									string.toast_already_running),
							Toast.LENGTH_SHORT).show();
					return;
				}
				// create a File object for the parent directory
				SimpleDateFormat s = new SimpleDateFormat("_yyyyMMdd_HHmmss");
				String date = s.format(new Date());

				String name = filename.getText().toString();
				String newname = null;
				try {
					// transcode to show Chinese character
					byte ptext[] = name.getBytes("ISO-8859-1");
					newname = new String(ptext, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				final String command = "screenrecord --time-limit "
						+ recordtime.getText().toString()
						+ " /sdcard/ScreenRecord/" + newname.replace(" ", "_")
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
							// Vibrate for 200 milliseconds
							vibrator.vibrate(200);
							if (checkbox.isChecked()) {
								finish();
							}
							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(
											getApplicationContext(),
											getResources()
													.getString(
															string.toast_start_recording),
											Toast.LENGTH_SHORT).show();
								}
							});
						} else {
							System.out.println("record not started");
							startedrecording = false;
							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(
											getApplicationContext(),
											getResources().getString(
													string.toast_access_denied),
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
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									string.toast_run_in_background),
							Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									string.toast_record_not_running),
							Toast.LENGTH_SHORT).show();
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
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			System.out.println("back pressed");
			if (startedrecording) {
				Toast.makeText(
						getApplicationContext(),
						getResources()
								.getString(string.toast_run_in_background),
						Toast.LENGTH_SHORT).show();
				finish();
			} else {
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(
								string.toast_record_not_running),
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
		return super.dispatchKeyEvent(event);
	}

}
