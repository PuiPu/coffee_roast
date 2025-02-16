package com.example.coffeetemperature.viewModel;

import com.example.coffeetemperature.model.TEST_mainModel;

public class mainViewModel {
    private TEST_mainModel mainModel = new TEST_mainModel();

    public void refresh() {
        mainModel.retreiveData(new TEST_mainModel().onDataReadyCallback() {
            @Override
            public void onDataReady(String data) {
                //TODO : exposes data to view
            }
        });
    }

}
