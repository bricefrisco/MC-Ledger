package com.ledger.backfill;

public class Record {
    private long timestamp;
    private String playerId;
    private String playerName;
    private double amount;
    private double balance;

    public long getTimestamp() {
        return timestamp;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalance() {
        return balance;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
