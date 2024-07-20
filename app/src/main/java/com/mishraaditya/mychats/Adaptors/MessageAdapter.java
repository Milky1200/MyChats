package com.mishraaditya.mychats.Adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;
import com.mishraaditya.mychats.Models.Message;
import com.mishraaditya.mychats.R;
import com.mishraaditya.mychats.databinding.ItemRecieveBinding;
import com.mishraaditya.mychats.databinding.ItemSentBinding;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter{

    final int ITEM_SENT=1;
    final int ITEM_RECEIVE=2;

    Context context;
    ArrayList<Message> messages;
    String senderRoom,receiverRoom;

    public MessageAdapter(Context context, ArrayList<Message> messages,String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom=senderRoom;
        this.receiverRoom=receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT){
            View view= LayoutInflater.from(context).inflate(R.layout.item_sent,parent,false);
            return new sentViewHolder(view);
        }else {
            View view= LayoutInflater.from(context).inflate(R.layout.item_recieve,parent,false);
            return new receiveViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message=messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return ITEM_SENT;
        }else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        int Reactions[]=new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };
        Message message=messages.get(position);
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(Reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if(holder.getClass()==sentViewHolder.class){
                sentViewHolder viewHolder=(sentViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(Reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }else {
                receiveViewHolder viewHolder=(receiveViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(Reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            message.setFeeling(pos);
            FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId())
                    .setValue(message);
            FirebaseDatabase.getInstance().getReference().child("chats").child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageId())
                    .setValue(message);
            return true;
        });


        if(holder.getClass()==sentViewHolder.class){
            sentViewHolder viewHolder=(sentViewHolder) holder;
            viewHolder.binding.message.setText(message.getMessage());
            if(message.getFeeling()>=0){
                //message.setFeeling(Reactions[(int)message.getFeeling()]);
                viewHolder.binding.feeling.setImageResource(Reactions[(int)message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);

            }
            else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }
            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
        }else{
            receiveViewHolder viewHolder=(receiveViewHolder) holder;
            viewHolder.binding.message.setText(message.getMessage());

            if(message.getFeeling()>=0){
                //message.setFeeling(Reactions[(int)message.getFeeling()]);
                viewHolder.binding.feeling.setImageResource(Reactions[(int)message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }
            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class sentViewHolder extends RecyclerView.ViewHolder {
        ItemSentBinding binding;
        public sentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding=ItemSentBinding.bind(itemView);
        }
    }

    public class receiveViewHolder extends RecyclerView.ViewHolder {
        ItemRecieveBinding binding;
        public receiveViewHolder(@NonNull View itemView) {
            super(itemView);
            binding=ItemRecieveBinding.bind(itemView);
        }
    }
}
