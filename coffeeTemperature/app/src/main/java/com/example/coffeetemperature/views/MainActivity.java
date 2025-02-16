package com.example.coffeetemperature.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import com.example.coffeetemperature.R;
import com.example.coffeetemperature.viewModel.TemperatureViewModel;
import com.example.coffeetemperature.model.BLEClientModel;
import com.example.coffeetemperature.model.TemperatureData;
import com.example.coffeetemperature.model.ConfirmDialog;
import com.example.coffeetemperature.model.TemperatureLineChart;
import com.example.coffeetemperature.model.Timer;

public class MainActivity extends AppCompatActivity {
    private TemperatureData temperatureData = new TemperatureData();
    private TemperatureLineChart tempLineChart;
    private com.example.coffeetemperature.model.TemperatureViewModel temperatureViewModel;
    private LifecycleOwner lifecycleOwner;
    private Button startButton, recordButton, stopButton;
    private Timer timer;
    // test
    private BLEClientModel bleClientModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        // initialize BLEClient (如果還沒啟動必須先跳出提醒)
//        initBLEClient();
        // initialize LineChart
        initLineChart();
        // initialize button
        initButton();
        // initialize timer
        initTimer();
        // initialize BLE button (TimeStamp: 2025.2.6 要在額外做一個 scan BLEserver 再進入 mainActivity 的 activity 嗎 ?)
        initBLEButton();
    }
    private void initBLEButton() {
        // BLE button
        TemperatureData temperatureEntries = new TemperatureData();
        bleClientModel = new BLEClientModel(this, temperatureEntries);
        findViewById(R.id.BLE_button).setOnClickListener(v -> bleClientModel.startScanning());
    }

    private void initLineChart() {
        tempLineChart = new TemperatureLineChart(this, temperatureData, findViewById(R.id.lineChart), lifecycleOwner);
        // Initialize ViewModel
        TemperatureViewModel factory = new TemperatureViewModel(temperatureData);
        /* timestamp:2025.2.10 現在卡在這裡沒解決，註解掉這行可以正常跑程式(但是功能沒有實現) */
        // temperatureViewModel = new ViewModelProvider(this, factory).get(TemperatureViewModel.class); // error here
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
        // 開始畫圖
        /* CODE here */
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