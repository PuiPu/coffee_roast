package com.example.coffeetemperature.model;

import android.os.Handler;
import android.widget.TextView;

public class Timer {
    private TextView timerTextView;
    private Handler handler = new Handler();
    private int secondsElapsed = 0;

    public Timer(TextView timerTextView) {
        this.timerTextView = timerTextView;
        secondsElapsed = 0;
        timerTextView.setText("00:00");
    }
    public void start() {
        // 每秒更新一次畫面
        handler.post(new Runnable() {
            @Override
            public void run() {
                // 計算經過的分鐘和秒數
                int minutes = secondsElapsed / 60;
                int seconds = secondsElapsed % 60;

                // 格式化時間並顯示
                String time = String.format("%02d:%02d", minutes, seconds);
                timerTextView.setText(time);

                // 累加秒數並延遲 1 秒再次執行
                secondsElapsed++;
                handler.postDelayed(this, 1000);
            }
        });
    }
    public void stop() {
        handler.removeCallbacksAndMessages(null);
    }
}
