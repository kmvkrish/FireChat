package com.example.android.firechat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Created by kmvkrish on 26-12-2016.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int FROM_ME = 1;
    private List<Message> mMessageList;

    public MessageAdapter(List<Message> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public int getItemViewType(int position) {
        if(mMessageList.get(position).getName().equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())){
            return FROM_ME;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MessageAdapter.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch(viewType){
            case 1:
                View v = inflater.inflate(R.layout.message_list_item_right, parent, false);
                viewHolder = new MessageAdapter.ViewHolder(v);
                break;
            default:
                View view = inflater.inflate(R.layout.message_llist_item_left, parent, false);
                viewHolder = new MessageAdapter.ViewHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);

        boolean isPhotoUrl = (message.getPhotoUrl() != null);

        if(isPhotoUrl){
            holder.messageTextView.setVisibility(View.GONE);
            holder.photoView.setVisibility(View.VISIBLE);
            Glide.with(holder.photoView.getContext())
                    .load(message.getPhotoUrl())
                    .into(holder.photoView);
        }else{
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.photoView.setVisibility(View.GONE);
            holder.messageTextView.setText(message.getText());
        }
        holder.nameTextView.setText(message.getName());
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView photoView;
        public TextView nameTextView;
        public TextView messageTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.photoView = (ImageView) itemView.findViewById(R.id.photoImageView);
            this.nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            this.messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
        }
    }
}
