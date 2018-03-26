package com.example.cho00054.androidlabs;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.cho00054.androidlabs.ChatWindow;
import com.example.cho00054.androidlabs.R;

/**
 * Created by cho00054 on 2018-03-25.
 */

public class MessageFragment extends Fragment {

        Context parent;
        String chatMessage;
        ChatWindow cw;
        Long id;
        boolean isTablet = false;

        public void onCreate(Bundle b)
        {
            super.onCreate(b);
            cw = new ChatWindow();
            Bundle getInfo = getArguments();
            chatMessage = getInfo.getString("chatItemMessage");
            id = getInfo.getLong("ID");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            //return super.onCreateView(inflater, container, savedInstanceState);
            View gui = inflater.inflate(R.layout.message_details, null);
            TextView tvID =(TextView) gui.findViewById(R.id.tvId);
            TextView tvMessage =(TextView) gui.findViewById(R.id.tvMessage);
            tvMessage.setText(chatMessage);
            tvID.setText("ID = " +id);

            Button buttonDel = (Button) gui.findViewById(R.id.buttonDelete);
            buttonDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isTablet){
                        cw.deleteMessage(id);
                        tvMessage.setText("");
                        tvID.setText("");

                    } else {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("Response", id);
                        Log.i("Database id; ", "" + id);
                        getActivity().setResult(99, resultIntent);
                        getActivity().finish();
                    }
                }
            });
            return gui;
        }

        public void setIsTablet(boolean isTablet){
            this.isTablet = isTablet;
        }
}
