/*
 * [SERVER]
 * 1. 確保手機支持 bluetooth peripheral
 * 2. 設定 BLE 權限 (AndroidMainifest.xml)
 * 3. 啟動 bluetooth service
 * 4. 設定 Gatt service & characteristic
 * 5. broadcast Gatt Service
 * 6. handle Gatt Event'
 * 7. stop broadcast and close Gatt service
 *
 * [CLIENT]
 * 1. make sure BLE is enabled & device support BLE (AndroidManifest.xml)
 * 2. 初始化 bluetooth object
 * 3. search BLE device
 * 4. connect to GATT service
 * 5. listen to Gatt event
 */

package com.example.coffeetemperature.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.data.Entry;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class BLEClient {
    private static final String TAG = "BLEClient";
    private static final String DEVICE_NAME = "ESP32_Temperature"; // ESP32 藍牙名稱
    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private Context context;
    private Queue<Entry> temperatureEntriesBuffer;
    private int timeIndex;

    public BLEClient(Context context) {
        this.context = context;
        initBluetooth();
        temperatureEntriesBuffer = new LinkedList<>();
        timeIndex = 0;
    }

    @SuppressLint("MissingPermission")
    private void initBluetooth() {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Device does not support Bluetooth");
            Toast.makeText(context, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    @SuppressLint("MissingPermission")
    public void startScanning() {
        if (!checkBluetoothPermissions()) {
            return;
        }

        if (bluetoothLeScanner == null) {
            Log.e(TAG, "BluetoothLeScanner is null");
            Toast.makeText(context, "BluetoothLeScanner is unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        bluetoothLeScanner.startScan(scanCallback);
        Log.d(TAG, "Started BLE scanning");
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device.getName() != null && device.getName().equals(DEVICE_NAME)) {
                Log.d(TAG, "Found target device: " + device.getName());
                bluetoothLeScanner.stopScan(this);
                connectToDevice(device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "BLE Scan failed with error code: " + errorCode);
        }
    };

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        if (!checkBluetoothPermissions()) {
            return;
        }

        bluetoothGatt = device.connectGatt(context, false, gattCallback);
        Log.d(TAG, "Connecting to GATT server...");
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.");
                /*
                 * 跑到這裡來是不是代表 gatt server 失敗，如果是這樣，導致 gatt server 連線失敗的原因是什麼
                 * 1. 藍牙設備不在附近 (超出距離)
                 * 2. 藍牙設備被關閉
                 * 3. 藍牙設備故障
                 * 4. GATT 連線不穩定
                 * 5. 程式邏輯錯誤 (例如：gatt 被提早關閉)
                 * 6. Server 端斷開連線
                 */

                gatt.close();
                bluetoothGatt = null;
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service == null) {
                    Log.e(TAG, "Service UUID not found!");
                    return;
                }

                BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                if (characteristic == null) {
                    Log.e(TAG, "Characteristic UUID not found!");
                    return;
                }

                if (!checkBluetoothPermissions()) {
                    return;
                }

                Log.d(TAG, "Found target characteristic: " + characteristic.getUuid());
                gatt.readCharacteristic(characteristic);
                gatt.setCharacteristicNotification(characteristic, true);
            } else {
                Log.e(TAG, "Service discovery failed with status: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                if (data != null) {
                    String temperatureStr = new String(data);
                    try {
                        float temperature = Float.parseFloat(temperatureStr);
                        temperatureEntriesBuffer.add(new Entry(timeIndex++, temperature));
                        Log.d(TAG, "Temperature read: " + temperature);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Failed to parse temperature: " + temperatureStr, e);
                    }
                }
            } else {
                Log.e(TAG, "Failed to read characteristic, status: " + status);
            }
        }
    };

    private boolean checkBluetoothPermissions() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        1);
            }

            Log.e(TAG, "Bluetooth permissions not granted");
            return false;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
            Log.d(TAG, "Disconnected and closed GATT");
        }
    }
}
