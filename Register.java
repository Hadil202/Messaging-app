package com.example.massengerapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Register extends AppCompatActivity {
    private EditText name ,mobile ,email;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://massengerapp-b7950-default-rtdb.firebaseio.com/");

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        final EditText name = findViewById(R.id.r_name);
        final EditText mobile = findViewById(R.id.r_mobile);
        final EditText email = findViewById(R.id.r_email);
        final AppCompatButton registerBtn = findViewById(R.id.r_registerBtn);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        //check if user alredy logged in
       if(!MemoryData.getData(this).isEmpty()){
           Intent intent = new Intent(Register.this,MainActivity.class);
           intent.putExtra("mobile",MemoryData.getData(this));
           intent.putExtra("name",MemoryData.getName(this));
           intent.putExtra("email","");
           startActivity(intent);
           finish();
       }
        registerBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   progressDialog.show();
                   final String nameTxt = name.getText().toString();
                   final String mobileTxt = mobile.getText().toString();
                   final String emailTxt = email.getText().toString();
                   if(nameTxt.isEmpty()||mobileTxt.isEmpty()||emailTxt.isEmpty()){
                       Toast.makeText(Register.this, "All Fields Required!!!", Toast.LENGTH_SHORT).show();
                       progressDialog.dismiss();
                   }else{
                       databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                               progressDialog.dismiss();
                               if(snapshot.child("users").hasChild(mobileTxt)){
                                   Toast.makeText(Register.this,"Mobile Already exists",Toast.LENGTH_SHORT).show();
                               }else{
                                   databaseReference.child("users").child(mobileTxt).child("email").setValue(emailTxt);
                                   databaseReference.child("users").child(mobileTxt).child("name").setValue(nameTxt);
                                   databaseReference.child("users").child(mobileTxt).child("profile_pic").setValue("");

                                   //save mobile to memory
                                   MemoryData.saveData(mobileTxt, Register.this);
                                   //save name to memory
                                   MemoryData.saveData(nameTxt,Register.this);

                                   Toast.makeText(Register.this,"Success",Toast.LENGTH_SHORT).show();
                                   Intent intent = new Intent(Register.this,MainActivity.class);
                                   intent.putExtra("mobile",mobileTxt);
                                   intent.putExtra("name",nameTxt);
                                   intent.putExtra("email",emailTxt);
                                   startActivity(intent);
                                   finish();
                               }
                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError error) {
                               progressDialog.dismiss();

                           }
                       });

                   }
               }
           });
    }
}