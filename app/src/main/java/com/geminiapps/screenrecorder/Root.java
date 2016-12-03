package com.geminiapps.screenrecorder;

/**
 * Created by peng on 12/3/2016.
 */

import java.io.File;

import com.geminiapps.screenrecorder.ExecShell.SHELL_CMD;


public class Root {

    public boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    public boolean checkRootMethod1() {
        System.out.println("method1");
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    public boolean checkRootMethod2() {
        System.out.println("method2");
        try {
            File file = new File("/system/app/Superuser.apk");
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean checkRootMethod3() {
        System.out.println("method3");
        return new ExecShell().executeCommand(SHELL_CMD.check_su_binary) != null;
    }
}
