/*
 * LineChart build down order (structure->smallToBig)
 * - ArrayList<Entry>
 *  - LineDataSet
 *   - Thread -> 每秒掃一次資料
 *   - LineData (multi-LineDataSet)
 *    - LineChart
 */

package com.example.coffeetemperature.views;

import android.content.Context;
import android.graphics.Color;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.coffeetemperature.utils.BLEClient;
import com.example.coffeetemperature.utils.TemperatureData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TemperatureLineChart {
    private com.github.mikephil.charting.charts.LineChart lineChart;
    private LineDataSet dataSet;
    private LineData lineData;
    private Description description;
    private TemperatureData temperatureData;

    private MutableLiveData<List<Entry>> entriesLiveData = new MutableLiveData<>();
//    public LiveData<List<Entry>> getEntriesLiveData() {
//        return entriesLiveData;
//    }
    private BLEClient bleClient;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private LifecycleOwner lifecycleOwner; // lifecycleOwner is to connect data to the UI


    public TemperatureLineChart(Context context, TemperatureData temperatureData, LineChart lineChart, LifecycleOwner lifecycleOwner) {
        this.lineChart = lineChart;
        this.lifecycleOwner = lifecycleOwner;
        this.temperatureData = temperatureData;
        bleClient = new BLEClient(context, temperatureData);
        setupEmptyLineChart();
        observeTemperatureChanges();
    }

    private void observeTemperatureChanges() { // timeStamp:2025.2.10 這裡有 error
        if (lifecycleOwner == null) { // 這樣處理就會過了嗎 ???
            // Handle the case where lifecycleOwner is null, e.g., log an error or return
            return;
        }
        // 使用 RxJava 監聽 BLEClient 的資料更新
        Disposable disposable = bleClient.getTemperatureObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    new Action() { // Timestamp:2025.2.10 這裡變成 anonymous function 就過了(lamda 語法不會過)，為什麼 ?
                        @Override
                        public void run() throws Throwable {
                            entriesLiveData.setValue(temperatureData.getTemperatureList()); // setValue =>(mainThread, frontend), postValue =>(childThread, backend)
                        }
                    },
                    Throwable::printStackTrace
                ); 
        compositeDisposable.add(disposable);

        entriesLiveData.observe(lifecycleOwner, new Observer<List<Entry>>() { // timeStamp:2025.2.10 這裡有 error(java.lang.NullPointerException: Attempt to invoke interface method 'androidx.lifecycle.Lifecycle androidx.lifecycle.LifecycleOwner.getLifecycle()' on a null object reference)
            @Override
            public void onChanged(List<Entry> entries) {
                updateChartData(entries);
            }
        });
    }

    private void setupEmptyLineChart() {
        updateChartData(null);
    }

    private void updateChartData(List<Entry> entries) {
        if (entries != null) {
            updateDataSet(entries);
        } else {
            // 初始化一個空的 data set
            dataSet = new LineDataSet(null, "Temperature");
        }
        updateLineData();
        updateLineChart();
        setLineChartProperties();
        show();
    }

    private void updateDataSet(List<Entry> entries) {
        if (dataSet == null) {
            dataSet = new LineDataSet(entries, "Temperature");
        } else {
            dataSet.setValues(entries);
            dataSet.notifyDataSetChanged();
        }
    }

    private void updateLineData() {
        this.lineData = new LineData(dataSet);
        lineData.notifyDataChanged();
    }

    private TemperatureLineChart updateLineChart() {
        lineChart.setData(lineData);
        return this;
    }

    private TemperatureLineChart setLineChartProperties() {
        // position
        this.lineChart.getDescription().setEnabled(false);
        this.lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        this.lineChart.getAxisRight().setEnabled(false);

        // color
        this.dataSet.setColor(Color.BLUE);
        this.dataSet.setDrawValues(false);
        return this;
    }

    private void show() { lineChart.invalidate(); }

    public void updateDescription(Description D) { this.description = D; }

    public void startDataCollection() {
        bleClient.startScanning();
    }

    public void stopDataCollection() {
        bleClient.stopScanning();
    }
}
