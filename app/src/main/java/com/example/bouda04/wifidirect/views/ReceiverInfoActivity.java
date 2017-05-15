package com.example.bouda04.wifidirect.views;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.bouda04.wifidirect.R;
import com.example.bouda04.wifidirect.framework.InfoService;
import com.example.bouda04.wifidirect.framework.MyApplication;
import com.example.bouda04.wifidirect.framework.PublisherService;
import com.example.bouda04.wifidirect.framework.ReceiverService;
import com.example.bouda04.wifidirect.model.Member;

import java.net.InetAddress;

public class ReceiverInfoActivity extends InfoActivity {
    private final String TAG = "ReceiverInfoActivity";


    @Override
    protected void onResume() {
        super.onResume();
        Intent i = getIntent();
        int role = i.getIntExtra("role", Member.SUBSCRIBER_ROLE);
        String pubName = i.getStringExtra("pubName");
        ((TextView)findViewById(R.id.tvPublisher)).setText(pubName);
        InetAddress ip = (InetAddress) i.getSerializableExtra("serverIP");
        Class serviceClass = ReceiverService.class;
        Intent intent = new Intent(this, serviceClass);
        if (!MyApplication.isMyServiceRunning(serviceClass)) {
            intent.putExtra("serverIP", ip);
            Log.d(TAG, "startingService " + serviceClass.getName());
            this.startService(intent);
        }
        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                infoService = ((InfoService.MyBinder) iBinder).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {}
        };
        bindService(intent, sc, Context.BIND_AUTO_CREATE);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.receiver_info_activity);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void handleServiceMessages(Intent intent) {

    }


}