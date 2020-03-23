package com.univer.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.univer.chat.viewholder.MessageViewHolder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String FB_STORAGE_PATH = "image/";
    public static final String FB_DATABASE_PATH = "image";
    public static final int REQUEST_CODE = 1337;

    private static final int MINE = 1;
    private static final int MINE_IMAGE = 2;
    private static final int NOT_MINE = 3;
    private static final int NOT_MINE_IMAGE = 4;
    public String fileType;
    private static final Integer SIGN_IN_CODE = 1337;
    private LinearLayout activity_main;
    private RecyclerView messages;
    private ImageView imageView;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private Uri imageUri;
    private String url;
    private StorageTask storageTask;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            if ("image".equals(fileType)) {
                storageReference = FirebaseStorage.getInstance().getReference().child("images");
                databaseReference = FirebaseDatabase.getInstance().getReference().child("labs-pmp").push();
                String key = databaseReference.getKey();
                final StorageReference filePath = storageReference.child(key + ".jpg");
                filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        url = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", databaseReference.getKey());
                            map.put("text", url);
                            map.put("type", "image");
                            map.put("username", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                            map.put("messageTime", new Date().getTime());
                            databaseReference.setValue(map);
                        }
                    }
                });
            }

        }
        fetch();
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
                                              map.put("type", "text");
                                              map.put("username", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                              map.put("messageTime", new Date().getTime());
                                              databaseReference.setValue(map);
                                              textField.setText("");
                                          }
                                      }
        );
        ImageButton attachButton = findViewById(R.id.btnAttach);
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence[] options = new CharSequence[]{
                        "Images"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select a file");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fileType = "image";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Select an image"), REQUEST_CODE);
                    }
                });
                builder.show();
            }
        });
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
                                        Long.valueOf(snapshot.child("messageTime").getValue().toString()),
                                        snapshot.child("type").getValue().toString());
                            }
                        })
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(options) {

            @Override
            public int getItemViewType(int position) {
                Message item = getItem(position);
                String username = item.getUsername();
                String type = item.getType();
                String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if (username.equals(currentUserEmail) && type.equals("text")) {
                    return MINE;
                } else if (username.equals(currentUserEmail) && type.equals("image")) {
                    return MINE_IMAGE;
                } else if (!username.equals(currentUserEmail) && type.equals("text")) {
                    return NOT_MINE;
                }
                return NOT_MINE_IMAGE;
            }

            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v;
                if (viewType == MINE) {
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_rigth, parent, false);
                    return new MessageViewHolder(v);
                } else if (viewType == NOT_MINE) {
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_left, parent, false);
                    return new MessageViewHolder(v);
                } else if (viewType == MINE_IMAGE) {
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.image_item_right, parent, false);
                    return new MessageViewHolder(v);
                }
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.image_item_left, parent, false);
                return new MessageViewHolder(v);
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, final int position, @NonNull Message model) {
                holder.setData(model, holder);
            }
        };
        adapter.startListening();
        messages.setAdapter(adapter);
    }
}