package com.example.coffeetemperature.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.coffeetemperature.model.BLEClientModel;

import io.reactivex.rxjava3.annotations.NonNull;

public class BLEClientViewModel extends AndroidViewModel {
    private BLEClientModel bleClientModel;
    public LiveData<String> bleLiveData;

    public BLEClientViewModel(@NonNull Application application) {
        super(application);
        // bleClientModel = new BLEClientModel();
        bleLiveData = bleClientModel.getBLELiveData();
    }
    public LiveData<String> getBLELiveData() { // 只負責接收資料跟通知 UI，邏輯由 model 決定，getBLELiveData 一個 model 跟 Viewmodel 之間的 interface
        return bleLiveData;
    }
    public void initBluetooth() {
        bleClientModel.initBluetooth();
    }
    public void startScanning() {
        bleClientModel.startScanning();
    }
    public void stopScanning() {
        bleClientModel.stopScanning();
    }
    public void connectToDevice() {
        bleClientModel.connectToDevice();
    }
    public void disconnect() {
        bleClientModel.disconnect();
    }
}
