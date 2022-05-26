package com.ledger.api.dtos;

import java.util.List;

public class PlayerBalanceResponse {
    private List<Object> data;
    private double start;
    private double current;

    public List<Object> getData() {
        return data;
    }

    public double getStart() {
        return start;
    }

    public double getCurrent() {
        return current;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public void setCurrent(double current) {
        this.current = current;
    }
}
