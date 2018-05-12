package com.messagingtest.android.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.messagingtest.android.MyApp;
import com.messagingtest.android.R;
import com.messagingtest.android.managers.events.AppEventsManager;
import com.messagingtest.android.managers.events.MessageEvent;
import com.messagingtest.android.managers.events.TypedObserver;
import com.messagingtest.android.services.JobInstanceIdService;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private MessagesAdapter adapter;
    private UUID key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MyApp app = MyApp.from(this);
        JobInstanceIdService.launch(app.getDispatcher(),
                FirebaseInstanceId.getInstance().getToken());

        this.adapter = new MessagesAdapter(this);
        final RecyclerView recycler = findViewById(R.id.recycler);
        recycler.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
        recycler.setAdapter(adapter);

        adapter.swapMessages(app.getOpenHelper().getMessages());
        this.key = AppEventsManager.SHARED.subscribeToEvent(MessageEvent.class, new TypedObserver<MessageEvent>() {
            @Override
            public void onUpdate(final MessageEvent event) {
                adapter.addMessage(event.message);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppEventsManager.SHARED.unsubscribe(key);
    }
}
