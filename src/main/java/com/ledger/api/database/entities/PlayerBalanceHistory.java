package com.ledger.api.database.entities;

import com.ledger.api.database.DatabaseFileName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "player_balance_history")
@DatabaseFileName("player_balance_history.db")
public class PlayerBalanceHistory {
    @DatabaseField(index = true)
    private Long timestamp;

    @DatabaseField(index = true)
    private String playerId;

    @DatabaseField
    private Double balance;

    public Long getTimestamp() {
        return timestamp;
    }

    public String getPlayerId() {
        return playerId;
    }

    public Double getBalance() {
        return balance;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
