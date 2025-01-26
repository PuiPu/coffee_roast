//package com.example.coffeetemperature.utils;
//
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothSocket;
//import android.content.pm.PackageManager;
//import android.os.Environment;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.List;
//import java.util.UUID;
//
//import android.bluetooth.BluetoothAdapter;
//
//import androidx.core.app.ActivityCompat;
//
//import com.github.mikephil.charting.data.Entry;
//
//public class BLEClient {
//    private static final String DEVICE_NAME = "ESP32_Sensor"; // ESP32 藍牙名稱
//    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID
//
//    private BluetoothAdapter bluetoothAdapter;
//    private BluetoothSocket bluetoothSocket;
//    private InputStream inputStream;
//
//    private List<Entry> temperatureEntries; // 儲存溫度數據
//    private static final String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
//    private static final String CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
//    private BluetoothGatt bluetoothGatt;
//
//    public void connectToDevice(BluetoothDevice device) {
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        bluetoothGatt = device.connectGatt(null, false, new BluetoothGattCallback() {
//            @Override
//            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//                BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
//                if (service != null) {
//                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
//                    if (characteristic != null) {
//                        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                            // TODO: Consider calling
//                            //    ActivityCompat#requestPermissions
//                            // here to request the missing permissions, and then overriding
//                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                            //                                          int[] grantResults)
//                            // to handle the case where the user grants the permission. See the documentation
//                            // for ActivityCompat#requestPermissions for more details.
//                            return;
//                        }
//                        gatt.setCharacteristicNotification(characteristic, true);
//                    }
//                }
//            }
//
//            @Override
//            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//                String receivedData = characteristic.getStringValue(0);
//                saveToCSV(receivedData);
//            }
//        });
//    }
//
//    private void saveToCSV(String data) {
//        String csvFilePath = Environment.getExternalStorageDirectory().getPath() + "/BLE_Data.csv";
//
//        try (FileWriter writer = new FileWriter(csvFilePath, true)) {
//            writer.append(data).append("\n");
//            writer.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
////    private void connectToESP32() {
////        new Thread(() -> {
////    private void connectToESP32() {
////        new Thread(() -> {
////            try {
////                // 搜尋配對的藍牙設備
////                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
////                BluetoothDevice esp32Device = null;
////
////                for (BluetoothDevice device : pairedDevices) {
////                    if (DEVICE_NAME.equals(device.getName())) {
////                        esp32Device = device;
////                        break;
////                    }
////                }
////
////                if (esp32Device == null) {
////                    runOnUiThread(() -> Toast.makeText(this, "ESP32 not found", Toast.LENGTH_SHORT).show());
////                    return;
////                }
////
////                // 建立藍牙 Socket 連線
////                bluetoothSocket = esp32Device.createRfcommSocketToServiceRecord(UUID_SPP);
////                bluetoothSocket.connect();
////
////                inputStream = bluetoothSocket.getInputStream();
////                runOnUiThread(() -> Toast.makeText(this, "Connected to ESP32", Toast.LENGTH_SHORT).show());
////
////                listenForData();
////            } catch (IOException e) {
////                e.printStackTrace();
////                runOnUiThread(() -> Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show());
////            }
////        }).start();
////    }
////
////    private void listenForData() {
////        new Thread(() -> {
////            byte[] buffer = new byte[1024];
////            int bytes;
////
////            while (true) {
////                try {
////                    if (inputStream == null) break;
////                    bytes = inputStream.read(buffer);
////                    String data = new String(buffer, 0, bytes).trim();
////
////                    // 確保接收到的是有效的溫度數據
////                    if (data.startsWith("Temperature:")) {
////                        String temperatureString = data.split(":")[1].trim().replace("°C", "");
////                        float temperature = Float.parseFloat(temperatureString);
////
////                        updateChart(temperature);
////                    }
////                } catch (IOException | NumberFormatException e) {
////                    e.printStackTrace();
////                    break;
////                }
////            }
////        }).start();
////    }
////
////    private void updateChart(float temperature) {
////        runOnUiThread(() -> {
////            // 新增一個溫度數據點
////            temperatureEntries.add(new Entry(timeIndex++, temperature));
////
////            // 通知數據集已更新
////            lineData.notifyDataChanged();
////            lineChart.notifyDataSetChanged();
////            lineChart.invalidate(); // 刷新圖表
////        });
////    }
////
////    @Override
////    protected void onDestroy() {
////        super.onDestroy();
////        try {
////            if (inputStream != null) inputStream.close();
////            if (bluetoothSocket != null) bluetoothSocket.close();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////    }
//// }