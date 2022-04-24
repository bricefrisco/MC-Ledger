package com.ledger.api.database.entities;

import com.ledger.api.database.DatabaseFileName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "player_balance")
@DatabaseFileName("player_balance.db")
public class PlayerBalance {
    @DatabaseField(id = true)
    private String playerId;

    @DatabaseField(index = true)
    private String playerName;

    @DatabaseField(index = true)
    private double balance;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
