package com.mishraaditya.mychats.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mishraaditya.mychats.Adaptors.MessageAdapter;
import com.mishraaditya.mychats.Models.Message;
import com.mishraaditya.mychats.R;
import com.mishraaditya.mychats.databinding.ActivityChatBinding;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    ArrayList<Message> messages;
    MessageAdapter adapter;
    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String name=getIntent().getStringExtra("name");
        String receiverUid=getIntent().getStringExtra("uid");
        String senderUid= FirebaseAuth.getInstance().getUid();

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        messages=new ArrayList<>();

        //current
        senderRoom=senderUid+receiverUid;
        receiverRoom=receiverUid+senderUid;

        database=FirebaseDatabase.getInstance();

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1:snapshot.getChildren()){
                            Message message=snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        adapter=new MessageAdapter(this,messages,senderRoom,receiverRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText=binding.messageBox.getText().toString();
                Date date=new Date();
                Message message=new Message(messageText,senderUid,date.getTime());
                message.setFeeling(-1);
                binding.messageBox.setText("");

                String token=database.getReference().push().getKey();

                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());

                database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(token)//generates a new value other wise it will over lap
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(token)
                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(getApplicationContext(),"Sent",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}