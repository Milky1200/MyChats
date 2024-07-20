package com.mishraaditya.mychats.Adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
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

    public MessageAdapter(Context context, ArrayList<Message> messages) {
        this.context = context;
        this.messages = messages;
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

        Message message=messages.get(position);
        if(holder.getClass()==sentViewHolder.class){
            sentViewHolder viewHolder=(sentViewHolder) holder;
            viewHolder.binding.message.setText(message.getMessage());
        }else{
            receiveViewHolder viewHolder=(receiveViewHolder) holder;
            viewHolder.binding.message.setText(message.getMessage());
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
