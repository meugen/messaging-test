package com.messagingtest.android.managers.events;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AppEventsManager {

    private static final String TAG = AppEventsManager.class.getSimpleName();

    public static final AppEventsManager SHARED = new AppEventsManager();

    private final Map<UUID, ObserverWrapper<?>> observers;
    final Handler handler;

    private AppEventsManager() {
        observers = new ArrayMap<>();
        handler = new Handler(Looper.getMainLooper());
    }

    @AnyThread
    public void post(final Object event) {
        Log.d(TAG, Thread.currentThread().getName());

        final List<ObserverWrapper<?>> wrappers;
        synchronized (this) {
            wrappers = new ArrayList<>(observers.values());
        }
        for (ObserverWrapper<?> wrapper : wrappers) {
            wrapper.update(this, event);
        }
    }

    @AnyThread
    public <T> UUID subscribeToEvent(
            final Class<T> clazz,
            final TypedObserver<T> observer) {
        UUID key = null;
        synchronized (this) {
            while (key == null || observers.containsKey(key)) {
                key = UUID.randomUUID();
            }
            final ObserverWrapper<T> impl = new ObserverWrapper<>(
                    clazz, observer, key);
            observers.put(key, impl);
        }
        return key;
    }

    @AnyThread
    public void unsubscribe(final UUID... keys) {
        unsubscribe(Arrays.asList(keys));
    }

    @AnyThread
    public synchronized void unsubscribe(final Collection<UUID> keys) {
        for (UUID key : keys) {
            Log.d(TAG, String.format("Unsubscribed %s", key));
            observers.remove(key);
        }
    }

    private static class ObserverWrapper<T> {

        private final Class<T> clazz;
        private final WeakReference<TypedObserver<T>> ref;

        private final UUID key;

        ObserverWrapper(
                final Class<T> clazz,
                final TypedObserver<T> observer,
                final UUID key) {
            this.clazz = clazz;
            this.key = key;
            this.ref = new WeakReference<>(observer);
        }

        @AnyThread
        void update(
                final AppEventsManager manager,
                final Object arg) {
            final TypedObserver<T> observer = ref.get();
            if (observer == null) {
                manager.unsubscribe(key);
                return;
            }
            if (clazz.isInstance(arg)) {
                manager.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        observer.onUpdate(clazz.cast(arg));
                    }
                });
            }
        }
    }
}
