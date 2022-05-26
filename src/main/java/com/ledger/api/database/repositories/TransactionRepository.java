package com.ledger.api.database.repositories;

import com.j256.ormlite.stmt.*;
import com.ledger.api.database.LedgerDB;
import com.ledger.api.database.entities.Transaction;
import com.ledger.api.dtos.TransactionsResponse;
import com.j256.ormlite.dao.Dao;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransactionRepository {
    private final Dao<Transaction, Long> dao;
    private static final long PAGE_SIZE = 35;

    public TransactionRepository(LedgerDB db) throws SQLException {
        this.dao = db.getDao(Transaction.class);
    }

    public void create(Transaction log) {
        try {
            dao.create(log);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void create(List<Transaction> logs) {
        try {
            dao.create(logs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<String> queryPlayersChanged(Long start, Long stop) {
        try {
            List<Transaction> ts = dao.queryBuilder().where().ge("timestamp", start).and()
                    .le("timestamp", stop).query();

            Set<String> result = new HashSet<>();
            for (Transaction t : ts) {
                result.add(t.getPlayerId());
            }

            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    public Transaction queryLastTransaction(String playerId, Long timestamp) {
        try {
            QueryBuilder<Transaction, Long> qb = dao.queryBuilder();

            qb.where().eq("playerId", playerId).and().le("timestamp", timestamp);
            qb.orderBy("timestamp", false);

            return qb.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double queryFirstBalance(String playerId) {
        try {
            QueryBuilder<Transaction, Long> qb = dao.queryBuilder();

            qb.where().eq("playerId", playerId);
            qb.orderBy("timestamp", true);

            Transaction t = qb.queryForFirst();
            if (t == null) return 0.00;
            return t.getBalance();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.00;
        }
    }

    public double queryBalanceAt(String playerId, Long timestamp) {
        try {
            QueryBuilder<Transaction, Long> qb = dao.queryBuilder();

            qb.where().eq("playerId", playerId).and().le("timestamp", timestamp);
            qb.orderBy("timestamp", false);

            Transaction t = qb.queryForFirst();
            if (t == null) return 0.00;
            return t.getBalance();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.00;
        }
    }

    public TransactionsResponse query(String playerId, Integer page, Boolean ascending, Long timestamp) {
        TransactionsResponse response = new TransactionsResponse();
        response.setPageSize(PAGE_SIZE);

        try {
            QueryBuilder<Transaction, Long> qb = dao.queryBuilder();
            Where<Transaction, Long> where = qb.where();

            if (playerId != null) {
                where.eq("playerId", playerId).and();
            }

            if (timestamp != null) {
                if (ascending) {
                    where.gt("timestamp", timestamp);
                } else {
                    where.lt("timestamp", timestamp);
                }
            }

            qb.orderBy("timestamp", ascending);

            response.setTotalCount(qb.countOf());

            qb.limit(PAGE_SIZE);
            qb.offset(page * PAGE_SIZE);

            List<Transaction> transactionLogs = qb.query();
            response.setTransactions(transactionLogs);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return response;
    }

    public void purgeBefore(long timestamp) {
        try {
            QueryBuilder<Transaction, Long> qb = dao.queryBuilder();
            qb.where().lt("timestamp", timestamp);
            List<Transaction> histories = qb.query();
            dao.delete(histories);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
