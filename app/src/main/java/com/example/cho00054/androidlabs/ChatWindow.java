package com.example.cho00054.androidlabs;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatWindow extends Activity {
    private ArrayList<String> list;
    private Context ctx;
    private SQLiteDatabase db;
    protected static final String ACTIVITY_NAME = "ChatWindow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        ctx = this;

        ListView listView = findViewById(R.id.chatView);
        Button sendButton = findViewById(R.id.sendButton);
        list = new ArrayList<>();

        ChatDatabaseHelper dbHelper = new ChatDatabaseHelper(ctx);
        db = dbHelper.getWritableDatabase();

        Cursor results = db.query(false, ChatDatabaseHelper.TABLE_NAME,
                new String[] { ChatDatabaseHelper.KEY_ID, ChatDatabaseHelper.KEY_MESSAGE},
                ChatDatabaseHelper.KEY_MESSAGE + " not null", null,
                null, null, null, null);

        results.moveToFirst();

        while(!results.isAfterLast()){
            String resultMessage = results.getString(results.getColumnIndex(ChatDatabaseHelper.KEY_MESSAGE));
            list.add(resultMessage);
            Log.i(ACTIVITY_NAME, "SQL MESSAGE:" + resultMessage);
            results.moveToNext();
        }
        results.moveToFirst();

        Log.i(ACTIVITY_NAME, "Cursor’s  column count =" + results.getColumnCount());
        for(int i = 0; i < results.getColumnCount(); i++) {
            Log.i(ACTIVITY_NAME, "Cursor’s  column name =" + results.getColumnName(i));
        }

        ChatAdapter messageAdapter = new ChatAdapter(ctx);
        listView.setAdapter(messageAdapter);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add new chat String to ArrayList<String>
                EditText textInput = findViewById(R.id.chatViewText);
                list.add(textInput.getText().toString());
                messageAdapter.notifyDataSetChanged(); //this restarts the process of getCount() & getView()

                // insert new chat String to db
                ContentValues newData = new ContentValues();
                newData.put(ChatDatabaseHelper.KEY_MESSAGE, textInput.getText().toString());
                db.insert(ChatDatabaseHelper.TABLE_NAME, null, newData);

                textInput.setText("");
          }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private class ChatAdapter extends ArrayAdapter<String>{
        public ChatAdapter(Context ctx) {
            super(ctx, 0);
        }

        @Override
        public int getCount(){
            return list.size();
        }

        @Override
        public String getItem(int position){
            return list.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
           //this returns the layout that will be positioned at the specified row in the list.
            LayoutInflater inflater = ChatWindow.this.getLayoutInflater();
            View result = null;
            if(position%2 == 0)
                result = inflater.inflate(R.layout.chat_row_incoming, null);
            else
                result = inflater.inflate(R.layout.chat_row_outgoing, null);
            TextView message = (TextView)result.findViewById(R.id.message_text);
            message.setText(getItem(position)); // get the string at position
            return result;
        }

        public long getId(int position){
            //This is the database id of the item at position
            return position;
        }
    }
}
