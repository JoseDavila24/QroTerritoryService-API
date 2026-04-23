package com.qroterritory.filter;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class CacheStatus {

    private boolean miss = false;

    public void markMiss() {
        this.miss = true;
    }

    public boolean isMiss() {
        return miss;
    }
}
