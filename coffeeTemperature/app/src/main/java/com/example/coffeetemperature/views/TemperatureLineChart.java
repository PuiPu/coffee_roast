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
import android.os.Handler;

import com.example.coffeetemperature.utils.BLEClient;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class TemperatureLineChart {
    private com.github.mikephil.charting.charts.LineChart lineChart;
    private LineDataSet dataSet;
    private LineData lineData;
    private Description description;
    private ArrayList<Entry> temperatureEntries;
    private int timeIndex; // for BLEClient, used to synchronize
    private BLEClient bleClient;
    private Handler handler = new Handler();
    private Runnable runnable;
    private static final long REFRESH_INTERVAL = 1000; // 1 second

    public TemperatureLineChart(Context context, LineChart lineChart) { // View::LineChart
        this.lineChart = lineChart;
        bleClient = new BLEClient(context); // context 需要宣告在 class 裡面嗎 ???
        // bleClient.connectToBLEServer();
        setupEmptyLineChart();

//        // Initialize and start the periodic data update
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                updateTemperatureEntries();
//                handler.postDelayed(this, REFRESH_INTERVAL);
//            }
//        };
//
//        handler.postDelayed(runnable, REFRESH_INTERVAL);

    }

    /* PURPOSE: receive dynamically changed data, then refresh lineChart */
    public void refreshLineChartWithNewTemperatureEntries(ArrayList<Entry> insertedEntries) {
        // insert entries
        insertTemperatureEntries(insertedEntries);
        // refresh
        this.updateDataSet(temperatureEntries)
                .updateLineData()
                    .updateLineChart()
                        .setLineChartProperties()
                            .show();
    }

//    public void stopUpdating() {
//        // 停止更新
//        handler.removeCallbacks(runnable);
//    }

    private void insertTemperatureEntries(ArrayList<Entry> insertedEntries) {
        this.temperatureEntries.addAll(insertedEntries);
    }

    private void setupEmptyLineChart() {
        this.updateDataSet(new ArrayList<>())
                .updateLineData()
                    .updateLineChart()
                        .setLineChartProperties()
                            .show();
    }

    private TemperatureLineChart updateDataSet(List<Entry> entries) {
        this.dataSet = new LineDataSet(entries, "Temperature");
        return this;
    }

    private TemperatureLineChart updateLineData() {
        this.lineData = new LineData(dataSet);
        return this;
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
        this.dataSet.setColor(Color.BLUE); // timestamp:2025.1.27 寫死 ?
        // this.dataSet.setValueTextColor(Color.TRANSPARENT); // 隱藏數值標籤 ?
        return this;
    }

    private void show() {
        this.lineChart.invalidate(); // 刷新圖表
    }

    public void updateDescription(Description D) {
        this.description = D;
    }

    public void updateTemperatureEntries() {
        Entry entry = new Entry(timeIndex++, bleClient.getTemperature(timeIndex)); // 2025.1.27 will cause race condition ?
        temperatureEntries.add(entry);
        // 通知數據集已更新
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate(); // 刷新圖表
    }

}
