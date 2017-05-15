package com.example.bouda04.wifidirect.views;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.example.bouda04.wifidirect.R;
import com.example.bouda04.wifidirect.framework.InfoService;
import com.example.bouda04.wifidirect.framework.MyApplication;
import com.example.bouda04.wifidirect.framework.PublisherService;
import com.example.bouda04.wifidirect.framework.ReceiverService;
import com.example.bouda04.wifidirect.model.Member;

import java.net.InetAddress;

public class PublisherInfoActivity extends InfoActivity {
    private final String TAG = "PublisherInfoActivity";


    @Override
    protected void onResume() {
        super.onResume();
        Intent i = getIntent();
        int role = i.getIntExtra("role", Member.SUBSCRIBER_ROLE);
        InetAddress ip = (InetAddress) i.getSerializableExtra("serverIP");
        Class serviceClass = PublisherService.class;
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(InfoService.CLIENTS_COUNT);

        this.registerReceiver(br, filter);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.publisher_info_activity);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void handleServiceMessages(Intent intent) {
        switch (intent.getAction()){
            case InfoService.CLIENTS_COUNT:
                TextView txt = (TextView)findViewById(R.id.tvListeners);
                int info = intent.getIntExtra("clients-count", 0);
                txt.setText(Integer.toString(info));
                break;
            default:
                break;
        }
    }


}