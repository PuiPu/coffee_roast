package com.example.coffeetemperature;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coffeetemperature.views.ConfirmDialog;
import com.example.coffeetemperature.views.temperatureLineChart;

public class MainActivity extends AppCompatActivity {

    private temperatureLineChart tempLineChart;
    private Button startButton, recordButton, stopButton;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 LineChart
        initLineChart();
        // 初始化 button
        initButton();
        // 初始化 timer
        initTimer();
    }
    private void initLineChart() {
        tempLineChart = new temperatureLineChart(
                findViewById(R.id.lineChart));
    }
    private void initButton() {
        startButton = findViewById(R.id.startButton);
        recordButton = findViewById(R.id.recordButton);
        stopButton = findViewById(R.id.stopButton);

        startButton.setEnabled(true);
        recordButton.setEnabled(true);
        stopButton.setEnabled(true);

        // 設置按鈕點擊事件
        startButton.setOnClickListener(v -> handleStart());
        recordButton.setOnClickListener(v -> handleRecord());
        stopButton.setOnClickListener(v -> handleStop()); // handleStop() 是當每次 被 click 瘩時候都會被呼叫
    }
    private void initTimer() {
        timer = new Timer(findViewById(R.id.timer));
        timer.start();
    }

    private void handleStart() {
        startButton.setEnabled(false);
        startButton.setAlpha(0.5F);
        // 開始計時
        timer.start();
    }

    private void handleRecord() {
        // 處理 Record 按鈕點擊事件
    }

    private void handleStop() {
        // confirm stopButton can pressed ?

        timer.stop();

        // double check (timestamp:2025.1.26 OK)
        ConfirmDialog CD = new ConfirmDialog(this,
                                getString(R.string.EN_startButtton_confirmDialog_title),
                                getString(R.string.EN_startButtton_confirmDialog_message));
        CD.show(result -> {
            if (result) {
                // User confirmed
                startActivity(new Intent(this, saveFileActivity.class));
            } else {
                // User canceled
                timer.start();
            }
        });

    }

    public class Timer {
        private TextView timerTextView;
        private Handler handler = new Handler();
        private int secondsElapsed = 0;
        public Timer(TextView timerTextView) {
            this.timerTextView = timerTextView;
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
            secondsElapsed = 0;
            timerTextView.setText("00:00");
//            startButton.setEnabled(true);
//            startButton.setAlpha(1.0F);
//            recordButton.setEnabled(true);
//            stopButton.setEnabled(true);
//            recordButton.setAlpha(1.0F);
//            stopButton.setAlpha(1.0F);
//            tempLineChart.clearData();
//            tempLineChart.invalidate();
//            timer.start();
        }
    }
}


