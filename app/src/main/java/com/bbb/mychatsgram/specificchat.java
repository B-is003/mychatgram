package com.bbb.mychatsgram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class specificchat extends AppCompatActivity {

    EditText mgetmessage;
    ImageButton msendmessagebutton;

    CardView msendmessagecardview;
    androidx.appcompat.widget.Toolbar mtoolbarofspecificchat;
    ImageView mimageviewofspecificuser,mcamera ;
    TextView mnameofspecificuser;

    ProgressDialog dialog;


    private String enteredmessage;
    Intent intent;
    String mrecievername, sendername, mrecieveruid, msenderuid;
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage storage;
    String senderroom, recieverroom;

    ImageButton mbackbuttonofspecificchat;

    RecyclerView messagerecyclerview;

    String currenttime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;

    MessagesAdapter messagesAdapter;
    ArrayList<Messages> messagesArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specificchat);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending image...");
        dialog.setCancelable(false);

        mgetmessage = findViewById(R.id.getmessage);
        msendmessagecardview = findViewById(R.id.carviewofsendmessage);
        msendmessagebutton = findViewById(R.id.imageviewsendmessage);
        mtoolbarofspecificchat = findViewById(R.id.toolbarofspecificchat);
        mnameofspecificuser = findViewById(R.id.Nameofspecificuser);
        mimageviewofspecificuser = findViewById(R.id.specificuserimageinimageview);
        mbackbuttonofspecificchat = findViewById(R.id.backbuttonofspecificchat);
        mcamera=findViewById(R.id.camera);

        messagesArrayList = new ArrayList<>();
        messagerecyclerview = findViewById(R.id.recyclerviewofspecific);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagerecyclerview.setLayoutManager(linearLayoutManager);
        messagesAdapter = new MessagesAdapter(specificchat.this, messagesArrayList,mrecieveruid);
        messagerecyclerview.setAdapter(messagesAdapter);


        intent = getIntent();

        setSupportActionBar(mtoolbarofspecificchat);
        mtoolbarofspecificchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Toolbar is Clicked", Toast.LENGTH_SHORT).show();


            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm a");


        msenderuid = firebaseAuth.getUid();
        mrecieveruid = getIntent().getStringExtra("receiveruid");
        mrecievername = getIntent().getStringExtra("name");

        Log.d("Exec343",msenderuid+ " intent "+mrecieveruid);
        senderroom = msenderuid + mrecieveruid;
        recieverroom = mrecieveruid + msenderuid;

        storage = FirebaseStorage.getInstance();

        DatabaseReference databaseReference = firebaseDatabase.getReference().child("chats").child(senderroom).child("messages");
        messagesAdapter = new MessagesAdapter(specificchat.this, messagesArrayList,mrecieveruid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Messages messages = snapshot1.getValue(Messages.class);
                    messages.setMessageID(snapshot1.getKey());
                    messagesArrayList.add(messages);
                }
                messagesAdapter = new MessagesAdapter(specificchat.this, messagesArrayList,mrecieveruid);
                messagerecyclerview.setAdapter(messagesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mbackbuttonofspecificchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });




        mnameofspecificuser.setText(mrecievername);
        String uri = intent.getStringExtra("imageuri");
        if (uri.isEmpty()) {
            Toast.makeText(getApplicationContext(), "null is recieved", Toast.LENGTH_SHORT).show();
        } else {
            Picasso.get().load(uri).into(mimageviewofspecificuser);
        }


        msendmessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                enteredmessage = mgetmessage.getText().toString();
                if (enteredmessage.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter message first", Toast.LENGTH_SHORT).show();
                } else {
                    Date date = new Date();
                    currenttime = simpleDateFormat.format(calendar.getTime());
                    Log.d("Exec343","Time :  "+simpleDateFormat.format(calendar.getTime())+ "  "+calendar.getTime());
                    Messages messages = new Messages(enteredmessage, firebaseAuth.getUid(), date.getTime(), currenttime);
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    firebaseDatabase.getReference().child("chats")
                            .child(senderroom)
                            .child("messages")
                            .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            firebaseDatabase.getReference()
                                    .child("chats")
                                    .child(recieverroom)
                                    .child("messages")
                                    .push()
                                    .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });



                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Data25", "Success");
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    messagesArrayList.clear();
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                        Messages messages = snapshot1.getValue(Messages.class);
                                        messagesArrayList.add(messages);
                                        messages.setMessageID(snapshot1.getKey());
                                        Log.d("Data25", messages.message);
                                    }
                                    messagesAdapter = new MessagesAdapter(specificchat.this, messagesArrayList,mrecieveruid);
                                    messagerecyclerview.setLayoutManager(linearLayoutManager);
                                    messagerecyclerview.setAdapter(messagesAdapter);
                                }

                                @Override

                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
//6ssP6obeiUQXQrrFmc7SskGMkrI26ssP6obeiUQXQrrFmc7SskGMkrI2
//6ssP6obeiUQXQrrFmc7SskGMkrI2 XLzcUm3iYePAtT2Enboj7hbv1x63
                    //-MeT00GmY7mbTLaIwkfS

                    mgetmessage.setText(null);


                }


            }
        });
        mcamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivity(intent);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 25){
            if(data == null){
                if(data.getData() !=null){
                  Uri selectedImage = data.getData();
                  Calendar calender = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats")
                            //.child(senderroom)
                            //.child("messages")
                            .child(calendar.getTimeInMillis() +"");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                      String filePath = uri.toString();
                                        Toast.makeText(specificchat.this,filePath,Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        }
                    });


                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (messagesAdapter != null) {
            messagesAdapter.notifyDataSetChanged();
        }
    }


}