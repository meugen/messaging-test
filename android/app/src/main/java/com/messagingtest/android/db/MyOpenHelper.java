package com.messagingtest.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.messagingtest.android.managers.events.AppEventsManager;
import com.messagingtest.android.managers.events.MessageEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyOpenHelper extends SQLiteOpenHelper {

    private static final String NAME = "myapp";
    private static final int VERSION = 2;

    public MyOpenHelper(final Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE messages (" +
                " id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                " message VARCHAR(200) NOT NULL," +
                " timestamp BIGINTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE messages");
        onCreate(db);
    }

    public List<MessageEntity> getMessages() {
        Cursor cursor = null;
        try {
            cursor = getWritableDatabase().rawQuery("SELECT message, timestamp FROM messages", new String[0]);

            final List<MessageEntity> result = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                final MessageEntity entity = new MessageEntity();
                entity.text = cursor.getString(0);
                entity.timestamp = new Date(cursor.getLong(1));
                result.add(entity);
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void addMessage(final String message) {
        final long timestamp = System.currentTimeMillis();

        final ContentValues values = new ContentValues();
        values.put("message", message);
        values.put("timestamp", timestamp);
        getWritableDatabase().insertOrThrow("messages",
                null, values);

        final MessageEntity entity = new MessageEntity();
        entity.text = message;
        entity.timestamp = new Date(timestamp);
        AppEventsManager.SHARED.post(new MessageEvent(entity));
    }
}
