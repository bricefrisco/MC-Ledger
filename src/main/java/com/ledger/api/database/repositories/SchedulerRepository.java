package com.ledger.api.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.ledger.api.database.LedgerDB;
import com.ledger.api.database.entities.HistoryType;
import com.ledger.api.database.entities.Scheduler;

import java.sql.SQLException;

public class SchedulerRepository {
    private final Dao<Scheduler, String> dao;

    public SchedulerRepository(LedgerDB db) throws SQLException {
        this.dao = db.getDao(Scheduler.class);
    }

    public Long getLastRun(String playerOrServer, HistoryType historyType) {
        try {
            Scheduler scheduler = dao.queryForId(historyType + "-" + playerOrServer);
            if (scheduler == null) return 0L;
            return scheduler.getLastRun();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public void setLastRun(String playerOrServer, HistoryType historyType, long lastRun) {
        Scheduler scheduler = new Scheduler();
        scheduler.setId(historyType + "-" + playerOrServer);
        scheduler.setLastRun(lastRun);
        try {
            dao.createOrUpdate(scheduler);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
