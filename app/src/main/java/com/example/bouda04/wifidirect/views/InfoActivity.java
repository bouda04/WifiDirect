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
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.bouda04.wifidirect.R;
import com.example.bouda04.wifidirect.framework.InfoService;
import com.example.bouda04.wifidirect.framework.MyApplication;
import com.example.bouda04.wifidirect.framework.PublisherService;
import com.example.bouda04.wifidirect.framework.ReceiverService;
import com.example.bouda04.wifidirect.framework.WifiP2PHandler;
import com.example.bouda04.wifidirect.model.Member;
import com.example.bouda04.wifidirect.model.WifiP2PProvider;

import java.net.InetAddress;

public class InfoActivity extends Activity {
    private final String TAG = "InfoActivity";
    public static final String NEW_INFO = "com.info-activity.new-info";
    InfoService infoService = null;
    BroadcastReceiver br =null;

    @Override
    protected void onPause() {
        try{
            if (br!=null){
                this.unregisterReceiver(br);
                br=null;
            }
        }catch(Exception e){}

        super.onPause();
    }

    @Override
    protected void onResume() {
        Intent i = getIntent();
        int role = i.getIntExtra("role", Member.SUBSCRIBER_ROLE);
        InetAddress ip = (InetAddress) i.getSerializableExtra("serverIP");
        Class serviceClass = (role == Member.SUBSCRIBER_ROLE)?
                ReceiverService.class: PublisherService.class;
        Intent intent = new Intent(this, serviceClass);
        if (!MyApplication.isMyServiceRunning(serviceClass)) {
            intent.putExtra("serverIP", ip);
            Log.d(TAG, "startingService " + serviceClass.getName());
            this.startService(intent);
        }
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                infoService = ((InfoService.MyBinder) iBinder).getService();
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {}
        }, Context.BIND_AUTO_CREATE);
        br = new InfoReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NEW_INFO);

        this.registerReceiver(br, filter);
        super.onResume();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ToggleButton tg = (ToggleButton) findViewById(R.id.tgStartStop);
        tg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked)
                    infoService.startInfo();
                else
                    infoService.stopInfo();
            }
        });
    }

    public class InfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            TextView txt = (TextView)findViewById(R.id.txtInfo);
            int info = intent.getIntExtra("info", -1);
            txt.setText(Integer.toString(info));
        }
    }
}