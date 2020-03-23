package com.univer.chat.viewholder;

import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.library.bubbleview.BubbleImageView;
import com.squareup.picasso.Picasso;
import com.univer.chat.Message;
import com.univer.chat.R;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    private TextView username;
    private TextView message;
    private BubbleImageView image;
    private TextView time;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        username = itemView.findViewById(R.id.messageUser);
        message = itemView.findViewById(R.id.messageText);
        time = itemView.findViewById(R.id.messageTime);
        image = itemView.findViewById(R.id.image);
    }

    public void setData(Message msg, MessageViewHolder viewHolder) {
        if (msg.getType().equals("text")) {
            username.setText(msg.getUsername());
            message.setText(msg.getText());
            time.setText(DateFormat.format("dd-MM-yyyy HH:mm:ss", msg.getMessageTime()));
        } else {
            username.setText(msg.getUsername());
            time.setText(DateFormat.format("dd-MM-yyyy HH:mm:ss", msg.getMessageTime()));
            Picasso.get().load(msg.getText()).into(viewHolder.image);
        }
    }

}