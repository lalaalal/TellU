package com.letmeinform.tellu;

import android.app.Activity;

public abstract class UIHandler<T> {
    private final Activity activity;

    public UIHandler(Activity activity) {
        this.activity = activity;
    }

    public void updateUI(T data) {
        activity.runOnUiThread(() -> runOnUIThread(data));
    }

    protected abstract void runOnUIThread(T data);
}
