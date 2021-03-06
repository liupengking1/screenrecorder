package com.geminiapps.screenrecorder;

/**
 * Created by peng on 12/3/2016.
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.Log;

public class ExecShell {

    private static String LOG_TAG = ExecShell.class.getName();

    public static enum SHELL_CMD {
        check_su_binary(new String[]{"/system/xbin/which", "su"});

        String[] command;

        SHELL_CMD(String[] command) {
            this.command = command;
        }
    }

    public ArrayList<String> executeCommand(SHELL_CMD shellCmd) {
        String line = null;
        ArrayList<String> fullResponse = new ArrayList<String>();
        Process localProcess = null;
        try {
            localProcess = Runtime.getRuntime().exec(shellCmd.command);
        } catch (Exception e) {
            return null;
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(
                localProcess.getInputStream()));
        try {
            while ((line = in.readLine()) != null) {
                Log.d(LOG_TAG, "--> Line received: " + line);
                fullResponse.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "--> Full response was: " + fullResponse);
        return fullResponse;
    }

    public boolean executeSuCommand(String[] cmd) {
        Process localProcess = null;
        try {
            localProcess = Runtime.getRuntime().exec("su");

            DataOutputStream outputStream = new DataOutputStream(
                    localProcess.getOutputStream());
            for (int i = 0; i < cmd.length; i++) {
                outputStream.writeBytes(cmd[i]);
                outputStream.flush();
            }
            localProcess.waitFor();
            System.out.println("return code:" + localProcess.exitValue());
            if (localProcess.exitValue() == 0) {
                // Code to run on success
                return true;
            } else {
                // Code to run on unsuccessful
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}