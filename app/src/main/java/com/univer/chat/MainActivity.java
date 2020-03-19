package com.univer.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.univer.chat.viewholder.MessageViewHolder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int MINE = 1;
    private static final int NOT_MINE = 0;
    private static final Integer SIGN_IN_CODE = 1;
    private LinearLayout activity_main;
    private RecyclerView messages;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(activity_main, "You are authorized", Snackbar.LENGTH_LONG).show();
                fetch();
            } else {
                Snackbar.make(activity_main, "You are not authorized", Snackbar.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setStackFromEnd(true);
        messages = findViewById(R.id.messagesList);

        messages.setLayoutManager(layout);
        messages.setHasFixedSize(true);
        activity_main = findViewById(R.id.activity_main);
        FloatingActionButton sendButton = findViewById(R.id.btnSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              EditText textField = findViewById(R.id.messageField);
                                              if (textField.getText().toString().equals("")) {
                                                  return;
                                              }
                                              DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("labs-pmp").push();
                                              Map<String, Object> map = new HashMap<>();
                                              map.put("id", databaseReference.getKey());
                                              map.put("text", textField.getText().toString());
                                              map.put("username", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                              map.put("messageTime", new Date().getTime());
                                              databaseReference.setValue(map);
                                              textField.setText("");
                                          }
                                      }
        );
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_CODE);
        } else {
            Snackbar.make(activity_main, "You are authorized", Snackbar.LENGTH_LONG).show();
            fetch();
        }
    }

    private void fetch() {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("labs-pmp");
        final FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(query, new SnapshotParser<Message>() {
                            @NonNull
                            @Override
                            public Message parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Message(snapshot.child("username").getValue().toString(),
                                        snapshot.child("text").getValue().toString(),
                                        Long.valueOf(snapshot.child("messageTime").getValue().toString()));
                            }
                        })
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {
            @Override
            public int getItemViewType(int position) {
                if (getItem(position).getUsername().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                    return MINE;
                }
                return NOT_MINE;
            }

            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = viewType == MINE ?
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.list_item_rigth, parent, false) :
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.list_item_left, parent, false);
                return new MessageViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, final int position, @NonNull Message model) {
                holder.setData(model);
            }
        };
        adapter.startListening();
        messages.setAdapter(adapter);
    }

}