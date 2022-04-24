package com.ledger.api.database.entities;

import com.ledger.api.database.DatabaseFileName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "server_balance_history")
@DatabaseFileName("server_balance_history.db")
public class ServerBalanceHistory {
    @DatabaseField(id = true)
    private Long timestamp;

    @DatabaseField
    private Integer numberOfPlayersTracked;

    @DatabaseField
    private double balance;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getNumberOfPlayersTracked() {
        return numberOfPlayersTracked;
    }

    public void setNumberOfPlayersTracked(Integer numberOfPlayersTracked) {
        this.numberOfPlayersTracked = numberOfPlayersTracked;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
