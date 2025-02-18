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

package com.example.coffeetemperature.model;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.mikephil.charting.data.Entry;

import java.util.UUID;

public class BLEClientModel {
    private static final String TAG = "BLEClient";
    private static final String DEVICE_NAME = "ESP32_Temperature"; // ESP32 藍牙名稱
    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private Context context;
    private int timeIndex;
    private Handler bleHandler;
    private HandlerThread handlerThread;
//    private TemperatureData temperatureData;

    /* TODO: TEST MVMM */
    private MutableLiveData<Entry> temperatureliveData = new MutableLiveData<>();
    public LiveData<Entry> getBLELiveData() { return temperatureliveData; }
    /* test END */

    public BLEClientModel(Context context) {
        this.context = context;
        // this.temperatureData = temperatureData;
        /* TimeStamp:2025.2.16 先把 initBluetooth 變成 public */
        // initBluetooth();

        timeIndex = 0;

        // 初始化 BLE 背景執行緒
        handlerThread = new HandlerThread("BLEThread");
        handlerThread.start();
        bleHandler = new Handler(handlerThread.getLooper());
    }

    @SuppressLint("MissingPermission")
    public void initBluetooth() {
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
//
//        // 檢查 Location 是否開啟
//        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
//                                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        if (!isLocationEnabled) {
//            new AlertDialog.Builder(context)
//                    .setTitle("Location Service Required")
//                    .setMessage("Please enable location service for Bluetooth scanning.")
//                    .setPositiveButton("OK", (dialog, which) -> context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
//                    .setNegativeButton("Cancel", null)
//                    .show();
//            return;
//        }

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
        if (!checkBluetoothPermissions()) return;

        if (bluetoothLeScanner == null) {
            Log.e(TAG, "BluetoothLeScanner is null");
            Toast.makeText(context, "BluetoothLeScanner is unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        bluetoothLeScanner.startScan(scanCallback);
        Log.d(TAG, "Started BLE scanning");
    }
    @SuppressLint("MissingPermission")
    public void stopScanning() {
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
            Log.d(TAG, "Stopped BLE scanning");
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceName = device.getName();

            Log.d(TAG, "Device found during scan: " + (deviceName != null ? deviceName : "Unnamed"));
            if (deviceName != null && deviceName.equals(DEVICE_NAME)) {
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
        if (!checkBluetoothPermissions()) return;

        Log.d(TAG, "Connecting to GATT server...");
        bleHandler.post(() -> {
            if (bluetoothGatt != null) {
                bluetoothGatt.close();
                bluetoothGatt = null;
            }
            bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
        });
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.");
                bleHandler.post(() -> gatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "Disconnected from GATT server, status: " + status);
                bleHandler.postDelayed(() -> connectToDevice(gatt.getDevice()), 3000);
                gatt.close();
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

                // Log.d(TAG, "Found target characteristic: " + characteristic.getUuid());
                gatt.setCharacteristicNotification(characteristic, true);

                // ✅ 2️⃣ 寫入 CCCD (Client Characteristic Configuration Descriptor)
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")); // <BLE29002.h>
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE); // 讓 ESP32 知道 client 需要接收 notify
                    gatt.writeDescriptor(descriptor);
                    Log.d(TAG, "CCCD Descriptor written: ENABLE_NOTIFICATION");
                } else {
                    Log.e(TAG, "CCCD Descriptor not found!");
                }
            } else {
                Log.e(TAG, "Service discovery failed with status: " + status);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            if (data != null) {
                String temperatureStr = new String(data);
                try {
                    float temperature = Float.parseFloat(temperatureStr);
                    // TODO: update temperature
                    temperatureliveData.setValue(new Entry(timeIndex++, temperature));
                    // temperatureLiveData.add(new Entry(timeIndex++, temperature));
                    Log.d(TAG, "Temperature updated: " + temperature);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Failed to parse temperature: " + temperatureStr, e);
                }
            }
            //Toast.makeText(context, "client 抓不到直", Toast.LENGTH_SHORT).show();
        }

//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                byte[] data = characteristic.getValue();
//                float temperature = bytesToFloat(data);
//                temperatureData.addTemperature(temperature);
//                temperatureLiveData.postValue(temperatureData.getTemperatureList());
//            }
//        }
    };

    private boolean checkBluetoothPermissions() {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // 如果沒有權限，請求權限
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                }, 1);
            } else {
                return true; // 已取得權限
            }
        }
        else{
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

