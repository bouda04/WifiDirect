package com.example.bouda04.wifidirect.model;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;


import com.example.bouda04.wifidirect.framework.MyApplication;
import com.example.bouda04.wifidirect.framework.WifiP2PHandler;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by bouda04 on 2/3/2017.
 */

public class WifiP2PProvider {


    Context context;
    WifiP2PHandler myWifiHandler;
    String OwnerName;
    int myRole;

    private Set<Member> peers = new HashSet<Member>();
    private EventHandler evh=null;
    private boolean isOwner;
    ServiceConnection sc;

    private final String TAG = "WifiP2PProvider";

    public WifiP2PProvider(Context context){
        this.context = context;
        BroadcastReceiver br = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2PHandler.WIFI_NEW_MEMBER);
        filter.addAction(WifiP2PHandler.WIFI_IAM_GO);
        filter.addAction(WifiP2PHandler.WIFI_IAM_CLIENT);
        context.registerReceiver(br, filter);
    }
    public void attach(final EventHandler evh, int role, final String OwnerName) {

        this.OwnerName = OwnerName;
        this.myRole = role;

        Intent intent = new Intent(context, WifiP2PHandler.class);
        if (!MyApplication.isMyServiceRunning(WifiP2PHandler.class)) {
            Log.d(TAG,"restarting the service... ");
            intent.putExtra("name", OwnerName);
            intent.putExtra("role", myRole);
            Log.d("TctTest", "startService");
            context.startService(intent);
        }
        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG,"got the binder from the service... ");
                myWifiHandler = ((WifiP2PHandler.WifiBinder) iBinder).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {}
        };
        context.bindService(intent,sc, Context.BIND_AUTO_CREATE);
        this.evh = evh;

    }

    public void restartDiscovery(){
//        stopDiscovery();
//        if (!isMyServiceRunning(WifiP2PHandler.class)) {
        peers.clear();
        if (evh != null)
            evh.clearMembersList();
        Log.d(TAG, "restarting the service... ");

        Intent intent = new Intent(context, WifiP2PHandler.class);
        intent.putExtra("name", OwnerName);
        intent.putExtra("role", myRole);
        context.startService(intent);
        }
 //   }

    public void stopDiscovery(){
        if (MyApplication.isMyServiceRunning(WifiP2PHandler.class)) {
            Log.d(TAG, "stoping the service... ");
            Intent intent = new Intent(context, WifiP2PHandler.class);
            if (sc!=null) {
                context.unbindService(sc);
                sc = null;
            }
            context.stopService(intent);
        }
    }

    public void connectToPeer(Member member){
        myWifiHandler.connect(member.getDevice(), myRole);
    }

    public void setDiscoveryHandler(EventHandler evh){
        this.evh = evh;
    }

    public void detach(){
        this.evh = null;
        if (sc!=null) {
            context.unbindService(sc);
            sc = null;
        }
    }

    public Set<Member> getPeers(){
        return this.peers;
    }

           public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            switch(intent.getAction()){
                case WifiP2PHandler.WIFI_NEW_MEMBER:
                    Member member = (Member) intent.getParcelableExtra("member");
                    if (!peers.contains(member) ) {
                      //  if (myRole == Member.PUBLISHER_ROLE)
                      //    myWifiHandler.connect(member.getDevice(), myRole);
                        if (evh != null)
                            evh.onNewDiscoveredMember(member);
                    }
                    peers.add(member);
                    break;
                case WifiP2PHandler.WIFI_IAM_GO:
                    Log.d(TAG, "I am the Group Owner... ");
                    if (evh != null)
                        evh.onGroupOwnerReady((InetAddress) intent.getSerializableExtra("GOaddress"));
                    break;
                case WifiP2PHandler.WIFI_IAM_CLIENT:
                    Log.d(TAG, "I am a client... ");
                    if (evh != null)
                        evh.onClientConnectionReady((InetAddress) intent.getSerializableExtra("GOaddress"));
                    break;
            }


        }
    }

    public interface EventHandler{
        public void onNewDiscoveredMember(Member member);
        public void clearMembersList();
        public void onGroupOwnerReady(InetAddress serverIP);
        public void onClientConnectionReady(InetAddress serverIP);
    }

}
