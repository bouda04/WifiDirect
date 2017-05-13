package com.example.bouda04.wifidirect.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.bouda04.wifidirect.R;

public class MainActivity extends Activity {
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        ((EditText)findViewById(R.id.edName)).setText(sp.getString("name", ""));

        ((Button)findViewById(R.id.btnGo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RadioGroup rgRole = (RadioGroup) findViewById(R.id.rgRole);
                Intent i;
                if (rgRole.getCheckedRadioButtonId() == R.id.radPub) {

                    i = new Intent(MainActivity.this, PublisherActivity.class);
                }
                else {

                    i = new Intent(MainActivity.this, SubscriberActivity.class);
                }
                String name = ((EditText)findViewById(R.id.edName)).getText().toString();
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("name", name);
                editor.commit();

                i.putExtra("name", name);
                startActivity(i);
            }
        });
    }

}
