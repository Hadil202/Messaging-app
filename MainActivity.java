package com.example.massengerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.massengerapp.messages.MessagesAdapter;
import com.example.massengerapp.messages.MessagesList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private final List<MessagesList> messagesLists = new ArrayList<>();
    private String mobile;
    private String email;
    private String name;
    private int unseenMessages = 0 ;
    private String lastMessage = "";

    private String chatKey ="" ;
    private boolean dataSet =false;
    private RecyclerView messagesRecyclerView ;
    private MessagesAdapter messagesAdapter;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://massengerapp-b7950-default-rtdb.firebaseio.com/");

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CircleImageView userProfilePic = findViewById(R.id.userProfilPic);

        messagesRecyclerView = findViewById(R.id.messgesRecyclerView);
        // get intent data from register
         mobile = getIntent().getStringExtra("mobile");
         name = getIntent().getStringExtra("name");
         email = getIntent().getStringExtra("email");
         messagesRecyclerView.setHasFixedSize(true);
         messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

         //set adapter to recyclerview
         messagesAdapter = new MessagesAdapter(messagesLists,MainActivity.this);


         messagesRecyclerView.setAdapter( messagesAdapter);

         ProgressDialog progressDialog = new ProgressDialog(this);
         progressDialog.setCancelable(false);
         progressDialog.setMessage("Loading...");
         progressDialog.show();


         //get profile pic from fire base
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String profilePicUrl = snapshot.child("users").child(mobile).child("profile_pic").getValue(String.class);

                if(profilePicUrl != null && !profilePicUrl.isEmpty()){
                    //set profile pic to circle image view
                    Picasso.get().load(profilePicUrl).into(userProfilePic);
                }
                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();

            }
        });
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesLists.clear();
                unseenMessages = 0;
                lastMessage = "";
                chatKey = "";

                for(DataSnapshot dataSnapshot :snapshot.child("users").getChildren()){
                    final String getMobile = dataSnapshot.getKey();
                    dataSet = false ;
                    if(!getMobile.equals(mobile)){
                        final String getName = dataSnapshot.child("name").getValue(String.class);
                        final String getProfilePic = dataSnapshot.child("profile_pic").getValue(String.class);


                        databaseReference.child("chat").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                int getChatCounts = (int)snapshot.getChildrenCount();
                                if(getChatCounts > 0){
                                    for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                                        final String getKey = dataSnapshot1.getKey();
                                        chatKey = getKey ;
                                        if(dataSnapshot1.hasChild("user_1") && dataSnapshot1.hasChild("user_2") && dataSnapshot1.hasChild("messages")){
                                            final String getUserOne = dataSnapshot1.child("user_1").getValue(String.class);
                                            final String getUserTwo = dataSnapshot1.child("user_2").getValue(String.class);
                                            if((getUserOne.equals(getMobile) && getUserTwo.equals(mobile)) ||getUserOne.equals(mobile) && getUserTwo.equals(getMobile)){
                                                for(DataSnapshot chatDataSnapshot : dataSnapshot1.child("messages").getChildren()){
                                                    final long getMessageKey = Long.parseLong(chatDataSnapshot.getKey());
                                                    final long getLastSeenMessage = Long.parseLong(MemoryData.getLastMsgTS(MainActivity.this,getKey));

                                                    lastMessage = chatDataSnapshot.child("msg").getValue(String.class);
                                                    if(getMessageKey > getLastSeenMessage){
                                                        unseenMessages++;

                                                    }

                                                }

                                            }
                                        }

                                    }
                                }  if (!dataSet){
                                    dataSet = true ;
                                    MessagesList messagesList = new MessagesList(getName,getMobile,lastMessage,getProfilePic, unseenMessages ,chatKey);
                                    messagesLists.add(messagesList);
                                    messagesAdapter.updateData(messagesLists);
                                }

                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}