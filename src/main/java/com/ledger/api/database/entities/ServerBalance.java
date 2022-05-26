package com.ledger.api.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "server_balances")
public class ServerBalance {
    @DatabaseField(id = true)
    private String id;
    @DatabaseField(index = true)
    private Long timestamp;
    @DatabaseField
    private Integer numberOfPlayersTracked;
    @DatabaseField
    private double balance;
    @DatabaseField(index = true)
    private HistoryType historyType;

    public String getId() {
        return id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Integer getNumberOfPlayersTracked() {
        return numberOfPlayersTracked;
    }

    public double getBalance() {
        return balance;
    }

    public HistoryType getHistoryType() {
        return historyType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setNumberOfPlayersTracked(Integer numberOfPlayersTracked) {
        this.numberOfPlayersTracked = numberOfPlayersTracked;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setHistoryType(HistoryType historyType) {
        this.historyType = historyType;
    }
}
