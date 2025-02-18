/*
 * LineChart build down order (structure->smallToBig)
 * - ArrayList<Entry>
 *  - LineDataSet
 *   - Thread -> 每秒掃一次資料
 *   - LineData (multi-LineDataSet)
 *    - LineChart
 */

package com.example.coffeetemperature.model;

import android.content.Context;
import android.graphics.Color;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

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
    private BLEClientModel bleClientModel;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private LifecycleOwner lifecycleOwner; // lifecycleOwner is to connect data to the UI


    public TemperatureLineChart(Context context, TemperatureData temperatureData, LineChart lineChart) {
        this.lineChart = lineChart;
        // this.lifecycleOwner = lifecycleOwner;
        this.temperatureData = temperatureData;
        bleClientModel = new BLEClientModel(context);
        setupEmptyLineChart();
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
        bleClientModel.startScanning();
    }

    public void stopDataCollection() {
        bleClientModel.stopScanning();
    }
}
