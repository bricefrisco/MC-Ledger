package com.ledger.api.dtos;

import java.sql.Date;
import java.util.List;

public class Session {
    private String id;
    private String playerId;
    private String playerName;
    private List<String> permissions;
    private Date expiresAt;

    public String getId() {
        return id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}
