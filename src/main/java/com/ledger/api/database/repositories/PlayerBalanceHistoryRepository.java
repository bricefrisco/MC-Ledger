package com.ledger.api.database.repositories;

import com.ledger.api.database.DaoCreator;
import com.ledger.api.database.entities.PlayerBalanceHistory;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PlayerBalanceHistoryRepository {
    private static Dao<PlayerBalanceHistory, Long> repository;

    public static void load() {
        try {
            repository = DaoCreator.getDaoAndCreateTable(PlayerBalanceHistory.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void create(PlayerBalanceHistory playerBalanceHistory) {
        try {
            repository.create(playerBalanceHistory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PlayerBalanceHistory getLast(String uuid) {
        try {
            QueryBuilder<PlayerBalanceHistory, Long> qb = repository.queryBuilder();
            qb.where().eq("playerId", uuid);
            qb.orderBy("timestamp", false);
            return qb.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void createAll(List<PlayerBalanceHistory> playerBalanceHistories) {
        try {
            List<PlayerBalanceHistory> changedPbhs = new ArrayList<>();
            for (PlayerBalanceHistory pbh : playerBalanceHistories) {
                PlayerBalanceHistory last = getLast(pbh.getPlayerId());
                if (last == null || !last.getBalance().equals(pbh.getBalance())) {
                    changedPbhs.add(pbh);
                }
            }
            repository.create(changedPbhs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<PlayerBalanceHistory> query(String uuid, int month) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.MONTH, month - 1);
        start.set(Calendar.DAY_OF_MONTH, 1);
        long startTime = start.getTimeInMillis();

        Calendar end = Calendar.getInstance();
        end.set(Calendar.MONTH, month);
        end.set(Calendar.DAY_OF_MONTH, 1);
        long endTime = end.getTimeInMillis();

        try {
            QueryBuilder<PlayerBalanceHistory, Long> qb = repository.queryBuilder();
            qb.where().eq("playerId", uuid).and().between("timestamp", startTime, endTime);
            qb.orderBy("timestamp", true);
            return qb.query();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void purgeBefore(long timestamp) {
        try {
            QueryBuilder<PlayerBalanceHistory, Long> qb = repository.queryBuilder();
            qb.where().lt("timestamp", timestamp);
            List<PlayerBalanceHistory> histories = qb.query();
            repository.delete(histories);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
