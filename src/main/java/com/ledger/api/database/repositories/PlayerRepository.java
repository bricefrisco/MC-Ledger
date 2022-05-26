package com.ledger.api.database.repositories;

import com.ledger.api.database.LedgerDB;
import com.ledger.api.database.entities.Player;
import com.ledger.api.dtos.PlayersResponse;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerRepository {
    private final Dao<Player, String> dao;
    private static final long PAGE_SIZE = 35;
    private static final long DAY = 24 * 60 * 60 * 1000;

    public PlayerRepository(LedgerDB db) throws SQLException {
        this.dao = db.getDao(Player.class);
    }

    public void create(List<Player> players) {
        try {
            dao.create(players);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createOrUpdate(Player player) {
        try {
            dao.createOrUpdate(player);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Player player) {
        try {
            dao.update(player);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getCurrentBalance(String uuid) {
        try {
            Player p = dao.queryForId(uuid);
            if (p == null) return 0.00;
            return p.getBalance();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.00;
        }
    }

    public String queryForUuid(String playerName) {
        try {
            QueryBuilder<Player, String> qb = dao.queryBuilder();
            qb.where().eq("playerName", playerName);
            Player pb = qb.queryForFirst();
            if (pb == null) return null;
            return pb.getPlayerId();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PlayersResponse query(int page) {
        PlayersResponse response = new PlayersResponse();

        try {
            QueryBuilder<Player, String> qb = dao.queryBuilder();
            qb.orderBy("balance", false);
            response.setPageSize(PAGE_SIZE);
            response.setTotalCount(qb.countOf());

            qb.offset(page * PAGE_SIZE);
            qb.limit(PAGE_SIZE);

            response.setBalances(qb.query());
            return response;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Player> queryActivePlayers() {
        long twoWeeksAgo = System.currentTimeMillis() - (DAY * 14);
        try {
            return dao.queryBuilder().where().gt("lastSeen", twoWeeksAgo).query();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Player> queryForAll() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
