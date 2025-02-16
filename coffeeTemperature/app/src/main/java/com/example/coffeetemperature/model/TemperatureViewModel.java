/* GOAL:
 *  1. view model handle all needs to be update in UI interface
 *  2. BENEFIT: isolate other model, make it more easy to test/use
 * LIVEDATA:
 *  1. LiveData: can only read => public
 *  2. MutableLiveData: can read/write => private
 */

package com.example.coffeetemperature.model;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class TemperatureViewModel extends ViewModel {
    private TemperatureData temperatureData = new TemperatureData();
    private LiveData<List<Float>> temperatureLiveData;

    public TemperatureViewModel(Context context) {
        temperatureLiveData = new MutableLiveData<>();
    }

    public LiveData<List<Float>> getTemperatureLiveData() {
        return temperatureLiveData;
    }
    public void setTemperatureLiveData(LiveData<List<Float>> temperatureLiveData) {
        this.temperatureLiveData = temperatureLiveData;
    }
}