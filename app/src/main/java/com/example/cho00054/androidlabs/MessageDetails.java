package com.example.cho00054.androidlabs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MessageDetails extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);

        MessageFragment mf = new MessageFragment();
        Bundle info = getIntent().getExtras();

        mf.setArguments(info);
        getFragmentManager().beginTransaction().add(R.id.chatWindowframe, mf).commit();

    }
}