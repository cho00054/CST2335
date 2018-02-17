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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        ctx = this;

        ListView listView = findViewById(R.id.chatView);
        Button sendButton = findViewById(R.id.sendButton);
        list = new ArrayList<>();
        ChatAdapter messageAdapter = new ChatAdapter( ctx );
        listView.setAdapter(messageAdapter);

        ChatDatabaseHelper dbHelper = new ChatDatabaseHelper(ctx);
        db = dbHelper.getWritableDatabase();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText textInput = findViewById(R.id.chatViewText);
                list.add(textInput.getText().toString());
                messageAdapter.notifyDataSetChanged(); //this restarts the process of getCount() & getView()
                textInput.setText("");

                ContentValues newData = new ContentValues();
                newData.put(ChatDatabaseHelper.KEY_MESSAGE, textInput.getText().toString());

                db.insert(ChatDatabaseHelper.TABLE_NAME, null, newData);

                Cursor results = db.query(false, ChatDatabaseHelper.TABLE_NAME,
                        new String[] {ChatDatabaseHelper.KEY_ID, ChatDatabaseHelper.KEY_MESSAGE},
                        null, null,
                        null, null, null, null);

                results.moveToFirst();
                while(!results.isAfterLast()){
                    String resultMessage = results.getString(results.getColumnIndex(ChatDatabaseHelper.KEY_MESSAGE));
                    Log.i("ChatWindow", "SQL MESSAGE:" + resultMessage);
                    results.moveToNext();
                }

                Log.i("ChatWindow", "Cursor’s  column count =" + results.getColumnCount());
                for(int i = 0; i < results.getColumnCount(); i++) {
                    Log.i("ChatWindow", "Cursor’s  column name =" + results.getColumnName(i));
                }

                results.moveToFirst();  //reset the cursor

                try {
                    SimpleCursorAdapter listAdapter =
                            new SimpleCursorAdapter(ctx, R.layout.activity_chat_window, results,
                                    new String[]{ChatDatabaseHelper.KEY_MESSAGE},
                                    new int[]{R.id.chatView}, 0);

                    listView.setAdapter(listAdapter); //populate the list with results\}
                }catch(Exception e)
                {
                    Log.e("Crash!", e.getMessage());
                }
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
