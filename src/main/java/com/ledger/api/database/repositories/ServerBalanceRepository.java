package com.ledger.api.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.ledger.api.database.LedgerDB;
import com.ledger.api.database.entities.HistoryType;
import com.ledger.api.database.entities.PlayerBalance;
import com.ledger.api.database.entities.ServerBalance;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class ServerBalanceRepository {
    private final Dao<ServerBalance, String> dao;
    private static final long DAY = 24 * 60 * 60 * 1000;

    public ServerBalanceRepository(LedgerDB db) throws SQLException {
        dao = db.getDao(ServerBalance.class);
    }

    public void create(ServerBalance serverBalance) {
        try {
            dao.create(serverBalance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createOrUpdate(ServerBalance sb) {
        try {
            dao.createOrUpdate(sb);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void create(List<ServerBalance> serverBalances) {
        try {
            dao.create(serverBalances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ServerBalance> query(HistoryType historyType) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        switch (historyType) {
            case DAILY -> cal.add(Calendar.DAY_OF_YEAR, -1);
            case WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, -7);
            case MONTHLY -> cal.add(Calendar.MONTH, -1);
            default -> cal.add(Calendar.YEAR, -200);
        }

        long timestamp = cal.getTimeInMillis();

        try {
            QueryBuilder<ServerBalance, String> qb = dao.queryBuilder();
            qb.where().eq("historyType", historyType).and().gt("timestamp", timestamp);
            qb.orderBy("timestamp", true);
            return qb.query();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ServerBalance queryBalanceAt(Long timestamp) {
        try {
            QueryBuilder<ServerBalance, String> qb = dao.queryBuilder();
            qb.where().eq("historyType", HistoryType.DAILY).and().le("timestamp", timestamp);
            qb.orderBy("timestamp", false);

            return qb.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void purge() {
        purgeDailyEntries();
        purgeWeeklyEntries();
        purgeMonthlyEntries();
    }

    private void purgeDailyEntries() {
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.add(Calendar.DAY_OF_YEAR, -1);
            long timestamp = cal.getTimeInMillis();

            List<ServerBalance> balances = dao.queryBuilder().where().eq("historyType", HistoryType.DAILY)
                    .and().lt("timestamp", timestamp).query();

            dao.delete(balances);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void purgeWeeklyEntries() {
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.add(Calendar.DAY_OF_YEAR, -7);
            long timestamp = cal.getTimeInMillis();

            List<ServerBalance> balances = dao.queryBuilder().where().eq("historyType", HistoryType.WEEKLY)
                    .and().lt("timestamp", timestamp).query();

            dao.delete(balances);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void purgeMonthlyEntries() {
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.add(Calendar.MONTH, -1);
            long timestamp = cal.getTimeInMillis();

            List<ServerBalance> balances = dao.queryBuilder().where().eq("historyType", HistoryType.MONTHLY)
                    .and().lt("timestamp", timestamp).query();

            dao.delete(balances);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
