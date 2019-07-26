package com.example.none.pigmanbox.application;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.example.none.pigmanbox.util.PathUtils;

/**
 * My application
 */
public class MyApplication extends Application {
    private static Context context;
    private static PackageInfo packageInfo;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        PackageManager pManager = context.getPackageManager();

        try {
            packageInfo = pManager.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        Utils.init(context);
        PathUtils.init(context);
    }

    public static Context getContext() {
        return context;
    }

    public static PackageInfo getPackageInfo() {
        return packageInfo;
    }
}
