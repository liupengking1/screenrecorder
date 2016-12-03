package com.geminiapps.screenrecorder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;


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

public class ScreenRecorderActivity extends Activity {
    Button startbtn;
    Button stopbtn;
    Button rateme;
    AutoCompleteTextView recordtime;
    AutoCompleteTextView filename;
    CheckBox checkbox;

    Process su = null;
    DataOutputStream outputStream;

    String pid;

    boolean isrooted = false;
    boolean startedrecording = false;
    Intent service_intent = new Intent("com.geminiapps.screenrecorder.service");

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
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

        setContentView(R.layout.activity_screen_recorder);
        startbtn = (Button) findViewById(R.id.startbtn);
        stopbtn = (Button) findViewById(R.id.stopbtn);
        recordtime = (AutoCompleteTextView) findViewById(R.id.recordtime);
        filename = (AutoCompleteTextView) findViewById(R.id.filename);
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        checkbox = (CheckBox) findViewById(R.id.checkBox1);
        if (!new Root().isDeviceRooted())
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.toast_not_rooted),
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
                            getResources().getString(R.string.toast_not_rooted),
                            Toast.LENGTH_LONG).show();
                if (startedrecording) {
                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(
                                    R.string.toast_already_running),
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
                                                            R.string.toast_start_recording),
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
                                                    R.string.toast_access_denied),
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
                                    R.string.toast_run_in_background),
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(
                                    R.string.toast_record_not_running),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /** Called before the activity is destroyed. */
    @Override
    public void onDestroy() {
        super.onDestroy();
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
                                .getString(R.string.toast_run_in_background),
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(
                                R.string.toast_record_not_running),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        return super.dispatchKeyEvent(event);
    }

}