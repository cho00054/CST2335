package com.example.cho00054.androidlabs;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChatWindow extends Activity {

    protected ArrayList<String> chatMessages = new ArrayList<>();
    protected ArrayList<Long> chatID = new ArrayList<>();
    private Context ctx;
   // private SQLiteDatabase db;
    protected static final String ACTIVITY_NAME = "ChatWindow";
    boolean isTablet = false;
    Cursor results;
    ChatAdapter messageAdapter;
    protected int clickedPosition;
    protected ChatDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        isTablet = (findViewById(R.id.chatWindowframe) != null); //check if FrameLayout exists
        ctx = this;

        ListView listView = (ListView) findViewById(R.id.chatView);
        Button sendButton = (Button) findViewById(R.id.sendButton);
        chatMessages = new ArrayList<>();

        dbHelper = new ChatDatabaseHelper(ctx);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        results = db.query(false, ChatDatabaseHelper.TABLE_NAME,
        new String[] { ChatDatabaseHelper.KEY_ID, ChatDatabaseHelper.KEY_MESSAGE},
        ChatDatabaseHelper.KEY_MESSAGE + " not null", null,
        null, null, null, null);

        results.moveToFirst();

        while(!results.isAfterLast()){
            chatMessages.add(results.getString(results.getColumnIndex(ChatDatabaseHelper.KEY_MESSAGE)));
            chatID.add(results.getLong(results.getColumnIndex(ChatDatabaseHelper.KEY_ID)));
            Log.i("ChatWindow", "SQL ID: "+ results.getInt(results.getColumnIndex(ChatDatabaseHelper.KEY_ID)));
            Log.i("ChatWindow", "SQL MESSAGE: "+ results.getString(results.getColumnIndex(ChatDatabaseHelper.KEY_MESSAGE)));
            results.moveToNext();
        }
        results.moveToFirst();

        Log.i(ACTIVITY_NAME, "Cursor’s  column count =" + results.getColumnCount());
        for(int i = 0; i < results.getColumnCount(); i++) {
            Log.i(ACTIVITY_NAME, "Cursor’s  column name =" + results.getColumnName(i));
        }

        messageAdapter = new ChatAdapter(ctx);
        listView.setAdapter(messageAdapter);

        final ContentValues newData = new ContentValues();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // add new chat String to ArrayList<String>'
                EditText textInput = (EditText) findViewById(R.id.chatViewText);
                newData.put(ChatDatabaseHelper.KEY_MESSAGE, textInput.getText().toString());
                Long newID =  db.insert(ChatDatabaseHelper.TABLE_NAME, "", newData);
                chatID.add(newID);
                chatMessages.add(textInput.getText().toString());
                messageAdapter.notifyDataSetChanged(); //this restarts the process of getCount() & getView()
                textInput.setText("");
          }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                clickedPosition = position;
                Bundle infoToPass = new Bundle();
                infoToPass.putLong("ID", id);
                infoToPass.putString("chatItemMessage", chatMessages.get(position));

                if(isTablet)
                {
                   MessageFragment mf = new MessageFragment();
                   mf.setArguments(infoToPass);
                   mf.setIsTablet(true);
                   getFragmentManager().beginTransaction().replace(R.id.chatWindowframe, mf).commit();
                } else {
                   Intent next = new Intent(ChatWindow.this, MessageDetails.class);
                   next.putExtras(infoToPass);
                   startActivityForResult(next, 25);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       //dbHelper.close();
    }

    public void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        if(responseCode == 99 ) { // delete message
            deleteMessage(data.getLongExtra("Response",0));
        }
    }

    public void deleteMessage(long id)
    {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(ChatDatabaseHelper.TABLE_NAME, ChatDatabaseHelper.KEY_ID + " = ?" ,
                new String[] { Long.toString(id) } );
        chatID.remove(clickedPosition);
        chatMessages.remove(clickedPosition);
        messageAdapter.notifyDataSetChanged();
    }

    private class ChatAdapter extends ArrayAdapter<String>{
        public ChatAdapter(Context ctx) {
            super(ctx, 0);
        }

        @Override
        public int getCount(){
            return chatMessages.size();
        }

        @Override
        public String getItem(int position){
            return chatMessages.get(position);
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

        public long getItemId(int position)
        {
            /*results.moveToPosition(position);
            Long itemID = results.getLong(results.getColumnIndex(ChatDatabaseHelper.KEY_ID)); //??
            return itemID;*/
            return chatID.get(position);
        }
    }
}
