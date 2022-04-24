package com.ledger.api.database.repositories;

import com.ledger.api.database.DaoCreator;
import com.ledger.api.database.entities.TransactionLog;
import com.ledger.api.dtos.TransactionsResponse;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import java.sql.SQLException;
import java.util.List;

public class TransactionLogRepository {
    private static Dao<TransactionLog, Long> repository;
    private static final long PAGE_SIZE = 35;

    public static void load() {
        try {
            repository = DaoCreator.getDaoAndCreateTable(TransactionLog.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void create(TransactionLog log) {
        try {
            repository.create(log);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static TransactionsResponse query(String playerId, Integer page, Boolean ascending, Long timestamp) {
        TransactionsResponse response = new TransactionsResponse();
        response.setPageSize(PAGE_SIZE);

        try {
            QueryBuilder<TransactionLog, Long> qb = repository.queryBuilder();
            if (playerId != null) {
                qb.where().eq("playerId", playerId);
            }

            if (timestamp != null) {
                if (ascending) {
                    qb.where().gt("timestamp", timestamp);
                } else {
                    qb.where().lt("timestamp", timestamp);
                }
            }

            qb.orderBy("timestamp", ascending);

            response.setTotalCount(qb.countOf());

            qb.limit(PAGE_SIZE);
            qb.offset(page * PAGE_SIZE);

            List<TransactionLog> transactionLogs = qb.query();
            response.setTransactions(transactionLogs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return response;
    }

    public static void purgeBefore(long timestamp) {
        try {
            QueryBuilder<TransactionLog, Long> qb = repository.queryBuilder();
            qb.where().lt("timestamp", timestamp);
            List<TransactionLog> histories = qb.query();
            repository.delete(histories);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
