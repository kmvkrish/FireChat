package com.example.android.firechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_PHOTO_PICKER = 2;

    private Message message;
    private List<Message> messageList = new ArrayList<>();
    private List<String> messageKeyList = new ArrayList<>();
    private RecyclerView mMessageListView;
    private MessageAdapter mMessageAdapter;

    private EditText mMessageEditText;
    private Button mMessageSendButton;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mMessageDatabaseReference;

    private FirebaseAuth mFirebaseAuth;

    private StorageReference mChatPhotoStorageReference;

    private ChildEventListener mMessageChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        mMessageListView = (RecyclerView) findViewById(R.id.messageListView);
        mMessageAdapter = new MessageAdapter(messageList);

        mMessageListView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mMessageListView.setItemAnimator(new DefaultItemAnimator());

        mMessageListView.setAdapter(mMessageAdapter);

        ImageButton photoPickerButton;
        FirebaseDatabase mFirebaseDatabase;
        FirebaseStorage mFirebaseStorage;

        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("Loading messages...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessageDatabaseReference = mFirebaseDatabase.getReference("messages");

        mFirebaseAuth = FirebaseAuth.getInstance();

        mFirebaseStorage = FirebaseStorage.getInstance();
        mChatPhotoStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageSendButton = (Button) findViewById(R.id.sendButton);
        photoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() > 0){
                    mMessageSendButton.setEnabled(true);
                }else{
                    mMessageSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mMessageSendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                message = new Message(mMessageEditText.getText().toString(), null, mFirebaseAuth.getCurrentUser().getDisplayName());
                mMessageDatabaseReference.push().setValue(message);
                mMessageEditText.setText("");
            }
        });

        photoPickerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.signOut){
            mFirebaseAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
            StorageReference photoRef = mChatPhotoStorageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUri = taskSnapshot.getDownloadUrl();
                            Message message = new Message(null, downloadUri.toString(), mFirebaseAuth.getCurrentUser().getDisplayName());
                            mMessageDatabaseReference.push().setValue(message);
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachChildEventListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachChildEventListener();
    }

    private void attachChildEventListener() {
        if(mMessageChildEventListener == null){
            mMessageChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String key = dataSnapshot.getKey();
                    messageKeyList.add(key);
                    messageList.add(dataSnapshot.getValue(Message.class));
                    mMessageAdapter.notifyItemInserted(mMessageAdapter.getItemCount());
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    int pos = messageKeyList.indexOf(dataSnapshot.getKey());
                    if(pos != -1){
                        messageKeyList.remove(pos);
                        messageList.remove(pos);
                        mMessageAdapter.notifyItemRemoved(pos);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mMessageDatabaseReference.addChildEventListener(mMessageChildEventListener);
        }
    }

    private void detachChildEventListener() {
        if(mMessageChildEventListener != null){
            mMessageDatabaseReference.removeEventListener(mMessageChildEventListener);
            messageList.clear();
            mMessageChildEventListener = null;
        }
    }

}
