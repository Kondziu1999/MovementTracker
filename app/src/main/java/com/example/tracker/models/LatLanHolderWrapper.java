package com.example.tracker.models;

import java.util.List;

public class LatLanHolderWrapper {
    private List<LatLanHolder> wrapper;

    public LatLanHolderWrapper(List<LatLanHolder> wrapper) {
        this.wrapper = wrapper;
    }

    public List<LatLanHolder> getWrapper() {
        return wrapper;
    }

    public void setWrapper(List<LatLanHolder> wrapper) {
        this.wrapper = wrapper;
    }
}
