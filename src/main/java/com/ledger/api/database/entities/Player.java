package com.ledger.api.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "players")
public class Player {
    @DatabaseField(id = true)
    private String playerId;

    @DatabaseField(index = true)
    private String playerName;

    @DatabaseField(index = true)
    private Double balance;

    @DatabaseField(index = true)
    private Long lastSeen;

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Double getBalance() {
        return balance;
    }

    public Long getLastSeen() {
        return lastSeen;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public void setLastSeen(Long lastSeen) {
        this.lastSeen = lastSeen;
    }
}
