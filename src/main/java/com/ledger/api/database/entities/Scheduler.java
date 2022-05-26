package com.ledger.api.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "scheduler")
public class Scheduler {
    @DatabaseField(id = true)
    private String id;
    @DatabaseField
    private Long lastRun;

    public String getId() {
        return id;
    }

    public Long getLastRun() {
        return lastRun;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLastRun(Long lastRun) {
        this.lastRun = lastRun;
    }
}
