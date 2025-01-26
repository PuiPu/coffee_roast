package com.example.coffeetemperature.views;

import android.app.AlertDialog;
import android.content.Context;

import java.util.function.Consumer;

public class confirmDialog {
    private String title;
    private String message;
    private boolean isConfirm;
    private Context context;

    public confirmDialog(Context context, String title, String message) {
        this.title = title;
        this.message = message;
        this.context = context;
        ConfirmationDialog(result -> {
            isConfirm = result;
        });
    }
    public boolean isConfirm() {
        return isConfirm;
    }
    private void ConfirmationDialog(Consumer<Boolean> callback) {
        // 建立 AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        // 設定 "確定" 按鈕
        builder.setPositiveButton("確定", (dialog, which) -> {
            callback.accept(true); // 回傳 true
        });

        // 設定 "取消" 按鈕
        builder.setNegativeButton("取消", (dialog, which) -> {
            callback.accept(false); // 回傳 false
        });

        builder.create().show();//創建並顯示AlertDialog
    }
}
