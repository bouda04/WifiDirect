package com.example.bouda04.wifidirect.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.bouda04.wifidirect.R;
import com.example.bouda04.wifidirect.model.Member;

import java.net.InetAddress;

public class PublisherActivity extends Activity implements MembersFrag.OnMembersInteractionListener {
    private int myRole = Member.PUBLISHER_ROLE;
    private InetAddress ip;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publisher);
        ((Button)findViewById(R.id.btnBroadcast)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PublisherActivity.this, InfoActivity.class);
                i.putExtra("role", myRole);
                i.putExtra("serverIP", ip);
                startActivity(i);
            }
        });
    }


    @Override
    public int getOwnerRole() {
        return myRole;
    }

    @Override
    public String getOwnerName() {
        return getIntent().getStringExtra("name");
    }

    @Override
    public void onWifiEstablished(InetAddress ipAddress) {
        this.ip= ipAddress;
        ((Button)findViewById(R.id.btnBroadcast)).setEnabled(true);
    }
}
