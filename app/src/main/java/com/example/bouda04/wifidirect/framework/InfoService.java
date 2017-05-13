package com.example.bouda04.wifidirect.framework;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public abstract class InfoService extends Service {
    protected Object lock = new Object();
    protected boolean goAhead = false;

    public InfoService() {}

    public void stopInfo() {
        synchronized(lock) {
            goAhead = false;
        }
    }

    public void startInfo() {
        synchronized(lock) {
            goAhead = true;
            lock.notifyAll();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public InfoService getService() {
            return InfoService.this;
        }
    }
}
