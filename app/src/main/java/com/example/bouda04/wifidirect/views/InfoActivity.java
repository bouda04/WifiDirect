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

public abstract class InfoActivity extends Activity {
    private final String TAG = "InfoActivity";
    protected InfoService infoService = null;
    protected ServiceConnection sc=null;
    protected BroadcastReceiver br =null;

    @Override
    protected void onPause() {
        try{
            if (br!=null){
                this.unregisterReceiver(br);
                br=null;
            }
        }catch(Exception e){}

        if (sc != null){
            unbindService(sc);
            sc = null;
            infoService = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        br = new InfoReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(InfoService.NEW_INFO);

        this.registerReceiver(br, filter);
        super.onResume();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    protected abstract void handleServiceMessages(Intent intent);

    public class InfoReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case InfoService.NEW_INFO:
                    TextView txt = (TextView)findViewById(R.id.txtInfo);
                    int info = intent.getIntExtra("info", -1);
                    txt.setText(Integer.toString(info));
                    break;
                default:
                    handleServiceMessages(intent);
                    break;
            }

        }
    }
}