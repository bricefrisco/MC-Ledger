package com.ledger.backfill;

public class PlayerIDHistoryResponse {
    private String name;
    private Long changedToAt;

    public String getName() {
        return name;
    }

    public Long getChangedToAt() {
        return changedToAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChangedToAt(Long changedToAt) {
        this.changedToAt = changedToAt;
    }
}
