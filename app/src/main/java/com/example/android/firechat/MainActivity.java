package com.example.android.firechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String ANONYMOUS = "Anonymous";
    private static final int RC_PHOTO_PICKER = 2;
    private String mUsername;

    private Message message;
    private ArrayList<Message> messageList = new ArrayList<>();
    private List<String> mMessageKeyList = new ArrayList<>();

    private MessageAdapter mMessageAdapter;
    private EditText mMessageEditText;
    private Button mMessageSendButton;
    private ImageButton photoPickerButton;

    private ProgressDialog mProgressDialog;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDatabaseReference;

    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotoStorageReference;

    private ChildEventListener mMessageChildEventListener;
    private ValueEventListener mMessageValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        ListView mMessageListView;

        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("Loading messages...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessageDatabaseReference = mFirebaseDatabase.getReference("messages");

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mFirebaseStorage = FirebaseStorage.getInstance();
        mChatPhotoStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        mUsername = mFirebaseUser.getDisplayName();

        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageSendButton = (Button) findViewById(R.id.sendButton);
        photoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);

        mMessageAdapter = new MessageAdapter(this, R.layout.message_llist_item_left, messageList);
        mMessageListView.setAdapter(mMessageAdapter);

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
                message = new Message(mMessageEditText.getText().toString(), null, mUsername);
                //messageList.add(message);
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
                            Message message = new Message(null, downloadUri.toString(), mUsername);
                            mMessageDatabaseReference.push().setValue(message);
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachChildEventListener();
        //attachSingleValueEVentListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachChildEventListener();
        //detachSingleValueEventListener();
    }

    private void attachChildEventListener() {
        if(mMessageChildEventListener == null){
            mMessageChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Message newMessage = dataSnapshot.getValue(Message.class);
                    mMessageAdapter.add(newMessage);
                    mMessageAdapter.notifyDataSetChanged();
                    if(mProgressDialog.isShowing()){
                        mProgressDialog.dismiss();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    mMessageAdapter.remove(dataSnapshot.getValue(Message.class));
                    mMessageAdapter.notifyDataSetChanged();

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
            mMessageAdapter.clear();
            mMessageChildEventListener = null;
        }
    }

    private void removeItemFromListAdapter(DataSnapshot dataSnapshot){
        Toast.makeText(MainActivity.this, "Before delete: " + mMessageAdapter.getCount(), Toast.LENGTH_LONG).show();
        Message newMessage = dataSnapshot.getValue(Message.class);
        mMessageAdapter.remove(newMessage);
        mMessageAdapter.notifyDataSetChanged();
        Toast.makeText(MainActivity.this, "After delete: " + mMessageAdapter.getCount(), Toast.LENGTH_LONG).show();
    }
}
