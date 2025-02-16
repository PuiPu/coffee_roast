package com.example.coffeetemperature.model;

import android.os.Handler;

public class TEST_mainModel {
    public void retreiveData(final onDataReadyCallback callback) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onDataReady("New Data");
            }
        }, 1500);
    }

    interface onDataReadyCallback {
        void onDataReady(String data);
    }
}
