package com.example.coffeetemperature.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.data.Entry;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class BLEClient {
    private static final String DEVICE_NAME = "ESP32_Temperature"; // ESP32s 藍牙名稱
    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID
    private static final String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    private static final String CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    private BluetoothDevice BLEServer;
    BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private Queue<Entry> temperatureEntriesBuffer; // 儲存溫度數據
    private BluetoothGatt bluetoothGatt;
    private Context context;
    private int timeIndex; // count timer (use to update)

    public BLEClient(Context context) {
        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter(); // once find bluetoothAdpater, close bluetoothAdapter to save energy consumption
        temperatureEntriesBuffer = new LinkedList<>();
        timeIndex = 0;
        BLEServer = null;
    }

    // ----------------------------------------- test start
    @SuppressLint("MissingPermission")
    public void connectToDevice(String deviceAddress) {
        // 根據裝置 MAC 地址取得 BluetoothDevice
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

        // 建立 GATT 連線 (會自動在背景執行緒執行)
        bluetoothGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    // 成功連線
                    Log.d("BLEClient", "Connected to GATT server. Discovering services...");
                    bluetoothGatt.discoverServices();
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    // 連線中斷
                    Log.d("BLEClient", "Disconnected from GATT server.");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // 尋找到服務，讀取或寫入 CHARACTERISTIC
                    BluetoothGattCharacteristic characteristic =
                            gatt.getService(UUID.fromString(SERVICE_UUID))
                                    .getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));

                    if (characteristic != null) {
                        // 讀取 Characteristic
                        gatt.readCharacteristic(characteristic);
                    } else {
                        Log.e("BLEClient", "Characteristic not found!");
                    }
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // 讀取成功
                    String value = new String(characteristic.getValue());
                    Log.d("BLEClient", "Characteristic value: " + value);
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLEClient", "Characteristic written successfully");
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }
    // ------------------------------------------------------ end

    public void connectToBLEServer() {
        try {
            // Users can check permission by themselves
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(context, "權限不夠", Toast.LENGTH_SHORT).show();
                return;
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            // match device
            for (BluetoothDevice device : pairedDevices) {
                if (DEVICE_NAME.equals(device.getName())) {
                    BLEServer = device;
                    break;
                }
            }
            // can't find device
            if (BLEServer == null) {
                Toast.makeText(context, "ESP32 not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // test
            Toast.makeText(context, "FIND ESP32", Toast.LENGTH_SHORT).show();

            try {
                // 建立藍牙 Socket 連線
                UUID serviceUuid = UUID.fromString(SERVICE_UUID);
                bluetoothSocket = BLEServer.createRfcommSocketToServiceRecord(serviceUuid);
//                bluetoothSocket = BLEServer.createRfcommSocketToServiceRecord(SERVICE_UUID);
                bluetoothSocket.connect();
            } catch (IOException e) {
                Log.d("BLEClient", "Socket connection failed: " + e.getMessage());
                throw new IOException("Socket connection failed: " + e.getMessage());
            }

            Toast.makeText(context, "socket build success", Toast.LENGTH_SHORT).show();

            inputStream = bluetoothSocket.getInputStream();
            Toast.makeText(context, "Connected to ESP32", Toast.LENGTH_SHORT).show();

            listenForData();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void listenForData() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    if (inputStream == null) break;
                    bytes = inputStream.read(buffer);
                    String data = new String(buffer, 0, bytes).trim();

                    // 確保接收到的是有效的溫度數據
                    if (data.startsWith("Temperature:")) {
                        String temperatureString = data.split(":")[1].trim().replace("°C", "");
                        float temperature = Float.parseFloat(temperatureString);

                        updateTemperatureEntriesBuffer(temperature);
                    }
                } catch (IOException | NumberFormatException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    private void updateTemperatureEntriesBuffer(float temperature) {
        // 新增一個溫度數據點
        temperatureEntriesBuffer.add(new Entry(timeIndex++, temperature));
    }

    public float getTemperature(int timeIndex) {
        float currentTemperature = 0;

        try {
            if (!temperatureEntriesBuffer.isEmpty()) {
                assert temperatureEntriesBuffer.peek() != null; // make sure that temperatureEntriesBuffer is not null
                currentTemperature = temperatureEntriesBuffer.peek().getY();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "No data available", Toast.LENGTH_SHORT).show();
        }

        return currentTemperature;
    }

}