package com.example.bouda04.wifidirect.controllers;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.bouda04.wifidirect.framework.MyApplication;
import com.example.bouda04.wifidirect.framework.PublisherService;
import com.example.bouda04.wifidirect.framework.ReceiverService;
import com.example.bouda04.wifidirect.framework.WifiP2PHandler;
import com.example.bouda04.wifidirect.model.Member;
import com.example.bouda04.wifidirect.model.WifiP2PProvider;
import com.example.bouda04.wifidirect.views.MembersFrag;

import java.net.InetAddress;
import java.util.Set;

/**
 * Created by bouda04 on 11/3/2017.
 */

public class MembersAdapter extends ArrayAdapter<Member> implements WifiP2PProvider.EventHandler{
    private final String TAG = "MembersAdapter";
    private WifiP2PProvider wifiProvider;
    private int role;
    private String ownerName;
    Context context;
    private MembersFrag.OnMembersInteractionListener listener;

    public MembersAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
        this.context=context;
        this.listener = (MembersFrag.OnMembersInteractionListener) context;
        this.role = listener.getOwnerRole();
        this.ownerName = listener.getOwnerName();
        wifiProvider = MyApplication.getWifiProvider();
        wifiProvider.attach(this, role, ownerName);
        addAll(wifiProvider.getPeers());
    }


    public void onConnectClick(){
        for (int i=0;i<getCount();i++){
   //         wifiController.connect(getItem(i).getDevice(), role==Member.PUBLISHER_ROLE);
        }

    }


    public void close(){
        wifiProvider.detach();
    }

    public void restartDiscovery(){
        wifiProvider.stopDiscovery();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiProvider.restartDiscovery();
            }
        }, 3000);

    }

    public void stopDiscovery(){
        wifiProvider.stopDiscovery();
    }
    @Override
    public void onNewDiscoveredMember(Member member) {
        add(member);
    }

    @Override
    public void clearMembersList() {
        clear();
    }

    @Override
    public void onGroupOwnerReady(InetAddress serverIP) {
        this.listener.onWifiEstablished(serverIP);
    }

    @Override
    public void onClientConnectionReady(InetAddress serverIP) {
        this.listener.onWifiEstablished(serverIP);
    }
}
