package com.example.android.firechat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kmvkrish on 26-12-2016.
 */

public class MessageAdapter extends ArrayAdapter<Message> {

    private final Context context;
    private final ArrayList<Message> messageArrayList;
    public MessageAdapter(Context context, int resource, List<Message> messageList){
        super(context, resource, messageList);
        this.context = context;
        this.messageArrayList = (ArrayList<Message>) messageList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View messageView;

        Message message = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(message.getName().equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())){
            convertView = (View)inflater.inflate(R.layout.message_list_item_right, parent, false);
        }else{
            convertView = (View)inflater.inflate(R.layout.message_llist_item_left, parent, false);
        }

        ImageView photoView = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);

        boolean isPhotoUrl = (message.getPhotoUrl() != null);

        if(isPhotoUrl){
            messageTextView.setVisibility(View.GONE);
            photoView.setVisibility(View.VISIBLE);
            Glide.with(photoView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoView);
        }else{
            messageTextView.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        nameTextView.setText(message.getName());

        return convertView;
    }
}
