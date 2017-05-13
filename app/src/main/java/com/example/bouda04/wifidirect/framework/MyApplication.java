package com.example.bouda04.wifidirect.framework;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.example.bouda04.wifidirect.model.WifiP2PProvider;

/**
 * Created by bouda04 on 9/3/2017.
 */

public class MyApplication extends Application {
    private static Context context;
    private static WifiP2PProvider wifiProvider;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        wifiProvider = new WifiP2PProvider(context);
        super.onCreate();
    }

    public static Context getAppContext(){
        return context;
    }
    public static WifiP2PProvider getWifiProvider(){
        return wifiProvider;
    }
    @Override
    public void onTerminate() {
       // wifiController.close();
        super.onTerminate();
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) MyApplication.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
