package com.example.coffeetemperature.views;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class temperatureLineChart {
    private com.github.mikephil.charting.charts.LineChart lineChart; // 折線圖
    private LineDataSet dataSet;
    private LineData lineData; // 多個 LineDataSet, 也就是多條線
    private Description description;
    private ArrayList<Entry> temperatureEntries;
    private int timeIndex = 0; // 時間索引

    public temperatureLineChart(LineChart lineChart) { // View::LineChart
        this.lineChart = lineChart;
        setupLineChart();
    }

    private void setupLineChart() {
        // 生成折線圖數據
        List<Entry> sinEntries = new ArrayList<>();
        List<Entry> cosEntries = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            sinEntries.add(new Entry(i, (float) Math.sin(i)));
            cosEntries.add(new Entry(i, (float) Math.cos(i)));
        }

        LineDataSet sinDataSet = new LineDataSet(sinEntries, "Sin");
        sinDataSet.setColor(Color.BLUE);
        sinDataSet.setValueTextColor(Color.TRANSPARENT); // 隱藏數值標籤

        LineDataSet cosDataSet = new LineDataSet(cosEntries, "Cos");
        cosDataSet.setColor(Color.RED);
        cosDataSet.setValueTextColor(Color.TRANSPARENT);

        LineData lineData = new LineData(sinDataSet, cosDataSet);

        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.invalidate(); // 刷新圖表
    }

    public LineChart getLineChart() {
        return lineChart;
    }
}
