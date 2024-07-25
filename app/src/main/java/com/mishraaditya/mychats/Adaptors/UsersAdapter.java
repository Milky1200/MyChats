package com.mishraaditya.mychats.Adaptors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mishraaditya.mychats.Activities.ChatActivity;
import com.mishraaditya.mychats.R;
import com.mishraaditya.mychats.Models.User;
import com.mishraaditya.mychats.databinding.RowConversationBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    Context context;
    ArrayList<User> usersData;

    public UsersAdapter(Context context, ArrayList<User> usersData) {
        this.context = context;
        this.usersData = usersData;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_conversation,parent,false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user=usersData.get(position);
//DataBase update for last two lines
        String senderId= FirebaseAuth.getInstance().getUid();
        String senderRoom=senderId+user.getUid();

        FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                                    long time = snapshot.child("lastMsgTime").getValue(Long.class);
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                    holder.binding.msgTime.setText(dateFormat.format(new Date(time)));
                                    holder.binding.lastMsg.setText(lastMsg);
                                }else{
                                    holder.binding.lastMsg.setText("Tap to chat");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

        holder.binding.username.setText(user.getName());
        Glide.with(context).load(user.getProfileImage()).placeholder(R.drawable.avatar)
                .into(holder.binding.profile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iNext=new Intent(context, ChatActivity.class);
                iNext.putExtra("name",user.getName());
                iNext.putExtra("image",user.getProfileImage());
                iNext.putExtra("uid",user.getUid());
                context.startActivity(iNext);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersData.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder{
        RowConversationBinding binding;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            binding=RowConversationBinding.bind(itemView);
        }
    }
}
