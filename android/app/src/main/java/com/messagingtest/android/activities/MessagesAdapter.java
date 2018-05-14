package com.messagingtest.android.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.messagingtest.android.R;
import com.messagingtest.android.db.MessageEntity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageHolder> {

    private final LayoutInflater inflater;
    private final DateFormat dateFormat;
    private List<MessageEntity> messages;

    MessagesAdapter(final Context context) {
        this.inflater = LayoutInflater.from(context);
        this.dateFormat = new SimpleDateFormat("dd MMMM, HH:mm:ss.SSS", Locale.ENGLISH);
        this.messages = new ArrayList<>();
    }

    public void swapMessages(final List<MessageEntity> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addMessage(final MessageEntity message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(
            @NonNull final ViewGroup parent,
            final int viewType) {
        final View view = inflater.inflate(R.layout.item_message, parent, false);
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageHolder holder, final int position) {
        final MessageEntity entity = messages.get(position);
        holder.message.setText(entity.text);
        holder.timestamp.setText(dateFormat.format(entity.timestamp));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageHolder extends RecyclerView.ViewHolder {

        final TextView message;
        final TextView timestamp;

        MessageHolder(final View itemView) {
            super(itemView);
            this.message = itemView.findViewById(R.id.message);
            this.timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}
