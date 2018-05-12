package com.messagingtest.android.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.messagingtest.android.R;

import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageHolder> {

    private final LayoutInflater inflater;
    private List<String> messages;

    public MessagesAdapter(final Context context) {
        this.inflater = LayoutInflater.from(context);
        this.messages = new ArrayList<>();
    }

    public void swapMessages(final List<String> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addMessage(final String message) {
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
        holder.message.setText(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageHolder extends RecyclerView.ViewHolder {

        private final TextView message;

        MessageHolder(final View itemView) {
            super(itemView);
            this.message = itemView.findViewById(R.id.message);
        }
    }
}
