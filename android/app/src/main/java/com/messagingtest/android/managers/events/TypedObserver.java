package com.messagingtest.android.managers.events;

import android.support.annotation.MainThread;

public interface TypedObserver<T> {

    @MainThread
    void onUpdate(T event);
}
