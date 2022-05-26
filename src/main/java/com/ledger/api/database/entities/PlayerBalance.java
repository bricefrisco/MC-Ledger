package com.ledger.api.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "player_balances")
public class PlayerBalance {
    @DatabaseField(id = true)
    private String id;
    @DatabaseField(index = true)
    private Long timestamp;
    @DatabaseField(index = true)
    private String playerId;
    @DatabaseField(index = true)
    private HistoryType historyType;
    @DatabaseField
    private Double balance;

    public String getId() {
        return id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getPlayerId() {
        return playerId;
    }

    public HistoryType getHistoryType() {
        return historyType;
    }

    public Double getBalance() {
        return balance;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setHistoryType(HistoryType historyType) {
        this.historyType = historyType;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
