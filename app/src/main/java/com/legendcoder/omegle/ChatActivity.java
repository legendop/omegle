package com.legendcoder.omegle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText messageEt;
    ImageView sendBtn;

    List<ModelChat> chatList;
    AdapterChat adapterChat;
    String roomId;
    String myId;
    //String hisID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.chat_recyclerView);
        messageEt = findViewById(R.id.messageEt);
        sendBtn=findViewById(R.id.sendBtn);

        Intent intent=getIntent();
        roomId=intent.getStringExtra("roomId");
        myId=intent.getStringExtra("myId");
        //hisID = intent.getStringExtra("hisID");

        Toast.makeText(this, "You are now connected to a random person...", Toast.LENGTH_SHORT).show();

        //Layout (LinearLayout) for RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recyclerview properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("Users");
        checkactive();
        readMeassage();

        //click button to send message
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get text from edit text
                String message = messageEt.getText().toString().trim();
                //check if text is empty or not
                if (TextUtils.isEmpty(message)) {
                    //text empty
                    Toast.makeText(ChatActivity.this, "Cannot send the empty message...", Toast.LENGTH_SHORT).show();
                } else {
                    //text not empty
                    sendMessage(message);
                }
                //reset edittext after sending message
                messageEt.setText("");
            }
        });



    }

    private void checkactive() {
        //CHECK coninuously if the roomclose happens
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("ActiveChats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String isactive= (String) snapshot.child(roomId).child("isactive").getValue();

                if(Objects.equals(isactive, "no")){
                    Toast.makeText(ChatActivity.this, "Other person has left the chat", Toast.LENGTH_SHORT).show();
                    newchat();
                }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void newchat() {
        messageEt.setEnabled(false);
        sendBtn.setClickable(false);


    }

    private void readMeassage() {
        chatList =new ArrayList<ModelChat>();
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Chats").child(roomId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);
                    chatList.add(chat);
                    adapterChat = new AdapterChat(ChatActivity.this,chatList);
                    adapterChat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(final String message) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myId);
        hashMap.put("message", message);
        databaseReference.child("Chats").child(roomId).push().setValue(hashMap);

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(roomId).child("myId");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


//        //create chatlist node/child in firebase database
//        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
//                .child(myUid)
//                .child(hisUid);
//        chatRef1.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()){
//                    chatRef1.child("id").setValue(hisUid);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
//                .child(hisUid)
//                .child(myUid);
//        chatRef2.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.exists()){
//                    chatRef2.child("id").setValue(myUid);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {

        case R.id.action_logout:
            exitroom();
            return(true);
        case R.id.action_newchat:
            exitroom();
            startActivity(new Intent(ChatActivity.this,MainActivity.class));
            return(true);

    }
        return(super.onOptionsItemSelected(item));
    }

    private void exitroom() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("ActiveChats").child(roomId);
        reference.child("isactive").setValue("no");
        newchat();
    }

    @Override
    public void onBackPressed() {
        exitroom();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        exitroom();
        super.onDestroy();
    }
}