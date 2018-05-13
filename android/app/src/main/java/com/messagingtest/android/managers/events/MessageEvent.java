package com.messagingtest.android.managers.events;

import com.messagingtest.android.db.MessageEntity;

public class MessageEvent {

    public final MessageEntity entity;

    public MessageEvent(final MessageEntity entity) {
        this.entity = entity;
    }
}
