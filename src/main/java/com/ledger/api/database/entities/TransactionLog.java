package com.ledger.api.database.entities;

import com.ledger.api.database.DatabaseFileName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "transaction_log")
@DatabaseFileName("transaction_log.db")
public class TransactionLog {
    @DatabaseField(index = true)
    private String playerId;

    @DatabaseField(index = true)
    private Long timestamp;

    @DatabaseField
    private String cause;

    @DatabaseField
    private Double amount;

    @DatabaseField
    private Double balance;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
