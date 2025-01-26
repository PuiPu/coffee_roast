package com.example.coffeetemperature;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Chronometer;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coffeetemperature.views.confirmDialog;
import com.example.coffeetemperature.views.temperatureLineChart;

public class MainActivity extends AppCompatActivity {

    private temperatureLineChart tempLineChart;
    private Button startButton, recordButton, stopButton;
    private Chronometer chronometer;

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
        recordButton.setEnabled(false);
        stopButton.setEnabled(false);

        // 設置按鈕點擊事件
        startButton.setOnClickListener(v -> handleStart());
        recordButton.setOnClickListener(v -> handleRecord());
        stopButton.setOnClickListener(v -> handleStop());
    }
    private void initTimer() {
        chronometer = findViewById(R.id.timer);
    }

    private void handleStart() {
        startButton.setEnabled(false);
        // 開始計時
        chronometer.start();
    }

    private void handleRecord() {
        // 處理 Record 按鈕點擊事件
    }

    private void handleStop() {
        // confirm stopButton can pressed
        if (!stopButttonIsStopable()) return;

        chronometer.stop();

        // double check
        confirmDialog CD = new confirmDialog(this, "stop", "are you sure to stop?");
        if (CD.isConfirm()) {
            startActivity(new Intent(this, saveFileActivity.class));
        }
        else {
//            startButton.setEnabled(true);
//            recordButton.setEnabled(false);
//            stopButton.setEnabled(false);
        }
    }
    private boolean stopButttonIsStopable() {
        // check start button is already pressed
        return startButton.isEnabled();
    }


}