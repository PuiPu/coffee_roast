package com.example.coffeetemperature.utils;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class TemperatureData {
    private List<Entry> temperatureList = new ArrayList<>();

    public List<Entry> getTemperatureList() {
        return temperatureList;
    }

    public void addTemperature(Entry temperature) {
        temperatureList.add(temperature);
    }
}
