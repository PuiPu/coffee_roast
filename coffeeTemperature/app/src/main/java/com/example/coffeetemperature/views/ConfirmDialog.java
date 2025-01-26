package com.example.coffeetemperature.views;

import android.app.AlertDialog;
import android.content.Context;

import com.example.coffeetemperature.R;

import java.util.function.Consumer;

public class ConfirmDialog {
    private final String title;
    private final String message;
    private final AlertDialog.Builder builder;

    public ConfirmDialog(Context context, String title, String message) {
        if (context == null || title == null || message == null) {
            throw new IllegalArgumentException("Context, title, and message cannot be null");
        }

        this.title = title;
        this.message = message;
        this.builder = new AlertDialog.Builder(context);
    }

    public void show(Consumer<Boolean> callback) {
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton(R.string.EN_accept, (dialog, which) -> callback.accept(true));
        builder.setNegativeButton(R.string.EN_deny, (dialog, which) -> callback.accept(false));

        builder.create().show();
    }
}
