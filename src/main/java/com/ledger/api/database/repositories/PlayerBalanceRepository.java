package com.ledger.api.database.repositories;

import com.ledger.api.database.DaoCreator;
import com.ledger.api.database.entities.PlayerBalance;
import com.ledger.api.dtos.PlayerBalancesResponse;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerBalanceRepository {
    private static Dao<PlayerBalance, String> repository;
    private static final long PAGE_SIZE = 35;

    public static void load() {
        try {
            repository = DaoCreator.getDaoAndCreateTable(PlayerBalance.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createOrUpdate(PlayerBalance playerBalance) {
        try {
            repository.createOrUpdate(playerBalance);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static PlayerBalancesResponse query(int page) {
        PlayerBalancesResponse response = new PlayerBalancesResponse();

        try {
            QueryBuilder<PlayerBalance, String> qb = repository.queryBuilder();
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

    public static List<PlayerBalance> queryForAll() {
        try {
            return repository.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
