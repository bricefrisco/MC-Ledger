package com.ledger.api.database.repositories;

import com.ledger.api.database.DaoCreator;
import com.ledger.api.database.entities.PlayerBalanceHistory;
import com.ledger.api.database.entities.ServerBalanceHistory;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ServerBalanceHistoryRepository {
    private static Dao<ServerBalanceHistory, Long> repository;

    public static void load() {
        try {
            repository = DaoCreator.getDaoAndCreateTable(ServerBalanceHistory.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ServerBalanceHistory getLast() {
        try {
            QueryBuilder<ServerBalanceHistory, Long> qb = repository.queryBuilder();
            qb.orderBy("timestamp", false);
            return qb.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void create(ServerBalanceHistory serverBalance) {
        try {
            ServerBalanceHistory last = getLast();
            if (last == null || last.getBalance() == serverBalance.getBalance()) {
                repository.create(serverBalance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<ServerBalanceHistory> query(int month) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.MONTH, month - 1);
        start.set(Calendar.DAY_OF_MONTH, 1);
        long startTime = start.getTimeInMillis();

        Calendar end = Calendar.getInstance();
        end.set(Calendar.MONTH, month);
        end.set(Calendar.DAY_OF_MONTH, 1);
        long endTime = end.getTimeInMillis();

        try {
            QueryBuilder<ServerBalanceHistory, Long> qb = repository.queryBuilder();
            qb.where().between("timestamp", startTime, endTime);
            return qb.query();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void purgeBefore(long timestamp) {
        try {
            QueryBuilder<ServerBalanceHistory, Long> qb = repository.queryBuilder();
            qb.where().lt("timestamp", timestamp);
            List<ServerBalanceHistory> histories = qb.query();
            repository.delete(histories);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
