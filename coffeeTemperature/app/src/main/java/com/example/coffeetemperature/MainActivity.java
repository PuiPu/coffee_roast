package com.example.coffeetemperature;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.coffeetemperature.utils.BLEClient;
import com.example.coffeetemperature.views.ConfirmDialog;
import com.example.coffeetemperature.views.TemperatureLineChart;
import com.example.coffeetemperature.views.Timer;

public class MainActivity extends AppCompatActivity {

    private TemperatureLineChart tempLineChart;
    private Button startButton, recordButton, stopButton;
    private Timer timer;
    // test
    BLEClient bleClient;

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

        // BLE button
        bleClient = new BLEClient(this);
        findViewById(R.id.BLE_button).setOnClickListener(v -> bleClient.startScanning());
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        try {
//            if (inputStream != null) inputStream.close();
//            if (bluetoothSocket != null) bluetoothSocket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    private void initLineChart() {
        tempLineChart = new TemperatureLineChart(this, findViewById(R.id.lineChart));
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
        if (startButton.isEnabled()) { // CAUTION: "True if this view is enabled" := "pressed button is TRUE"
            Toast.makeText(this, R.string.EN_press_start_button_first_caution, Toast.LENGTH_SHORT).show();
            return;
        }

        // stop timing
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
}