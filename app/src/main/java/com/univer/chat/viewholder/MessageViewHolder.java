package com.univer.chat.viewholder;

import android.text.format.DateFormat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.univer.chat.Message;
import com.univer.chat.R;

import lombok.Getter;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    private TextView username;
    private TextView message;
    private TextView time;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        username = itemView.findViewById(R.id.messageUser);
        message = itemView.findViewById(R.id.messageText);
        time = itemView.findViewById(R.id.messageTime);
    }

    public void setData(Message msg){
        username.setText(msg.getUsername());
        message.setText(msg.getText());
        time.setText(DateFormat.format("dd-MM-yyyy HH:mm:ss", msg.getMessageTime()));
    }

}