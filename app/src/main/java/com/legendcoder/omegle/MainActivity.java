package com.legendcoder.omegle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public TextView textView;
    public Button button;
    FirebaseDatabase firebaseDatabase;
    public static String ID;
    public static String hisID;
    public static String mycount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        button=findViewById(R.id.button);
        textView=findViewById(R.id.textView);
        String userId = (String) userid();
        ID=userId;

        //action bar
        ActionBar actionBar= getSupportActionBar();
        actionBar.setTitle("Register Activity");

        //backbutton
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createqueue(userId);
                Toast.makeText(MainActivity.this, "Searching for a random person...", Toast.LENGTH_SHORT).show();
                button.setClickable(false);
            }
        });


        //CHECK coninuously if the match happens
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("Users");

        Query query = databaseReference.orderByChild("UserId").equalTo(userId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //checkc until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    String roomId =""+ds.child("value").getValue();
                    if(!roomId.equals("idle")){
                        if(!roomId.equals("null")){
                            Intent intent=new Intent(MainActivity.this,ChatActivity.class);
                            intent.putExtra("roomId",roomId);
                            intent.putExtra("myId",userId);
                            //intent.putExtra("hisID",hisID);
                            startActivity(intent);
                            finish();
                        }

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public CharSequence userid() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        String userId = generatedString;
        return userId;
    }

    public void createqueue(String userId){
        //firebase datatbase
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //store user data under "Users"
        DatabaseReference databaseReference = database.getReference("Users");

        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    if((task.getResult().exists())){
                        DataSnapshot dataSnapshot=task.getResult();

                        String count=(String.valueOf(dataSnapshot.child("Count").getValue()));
                        mycount=count;
                        Integer newcount=Integer.parseInt(count)+1;
                        databaseReference.child("Count").setValue(newcount.toString());
                        DatabaseReference newreference=database.getReference("Users").child(count);
                        newreference.child("UserId").setValue(userId);
                        newreference.child("value").setValue("idle");
                        String hisCount= String.valueOf(Integer.parseInt(count)-1);
                        checkmatch(userId,hisCount);

                    }else{

                    }

                }else {

                }
            }
        });
    }

    private void checkmatch(String userId, String count) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = database.getReference("Users");
        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    if((task.getResult().exists())){
                        DataSnapshot dataSnapshot=task.getResult();
                        String isidle= (String) dataSnapshot.child(count).child("value").getValue();

                        if(Objects.equals(isidle, "idle")){
                            hisID = (String) dataSnapshot.child(count).child("UserId").getValue();
                            createroom(count);
                        }

                    }else{

                    }

                }else {

                }
            }
        });
    }

    private void createroom(String count) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference databaseReference = database.getReference("Users");
        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if ((task.getResult().exists())) {
                        DataSnapshot dataSnapshot = task.getResult();
                        String mycount = String.valueOf(Integer.parseInt(count) + 1);
                        String room = (String) userid();

                        DatabaseReference hisreference = database.getReference("Users").child(count);
                        hisreference.child("value").setValue(room);
                        DatabaseReference myreference = database.getReference("Users").child(mycount);
                        myreference.child("value").setValue(room);

                    } else {

                    }

                } else {

                }
            }
        });
    }

    @Override
    protected void onPause() {
        removefromqueue();
        super.onPause();
    }

    private void removefromqueue() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference hisreference = database.getReference("Users").child(mycount);
        hisreference.child("value").setValue("null");


    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}