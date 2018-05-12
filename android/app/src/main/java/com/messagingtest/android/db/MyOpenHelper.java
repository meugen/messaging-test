package com.messagingtest.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.messagingtest.android.managers.events.AppEventsManager;
import com.messagingtest.android.managers.events.MessageEvent;

import java.util.ArrayList;
import java.util.List;

public class MyOpenHelper extends SQLiteOpenHelper {

    private static final String NAME = "myapp";
    private static final int VERSION = 1;

    public MyOpenHelper(final Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE messages (" +
                " id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                " message VARCHAR(200) NOT NULL)");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE messages");
        onCreate(db);
    }

    public List<String> getMessages() {
        Cursor cursor = null;
        try {
            cursor = getWritableDatabase().rawQuery("SELECT message FROM messages", new String[0]);

            final List<String> result = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0));
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void addMessage(final String message) {
        final ContentValues values = new ContentValues();
        values.put("message", message);
        getWritableDatabase().insertOrThrow("messages",
                null, values);

        AppEventsManager.SHARED.post(new MessageEvent(message));
    }
}
