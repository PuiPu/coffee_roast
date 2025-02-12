package com.example.coffeetemperature.model;

import com.example.coffeetemperature.utils.TemperatureData;

public class TemperatureViewModelFactory {
    private TemperatureData temperatureData;

    public TemperatureViewModelFactory(TemperatureData temperatureData) {
        this.temperatureData = temperatureData;
    }

}
