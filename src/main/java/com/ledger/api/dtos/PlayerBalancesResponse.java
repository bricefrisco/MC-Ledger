package com.ledger.api.dtos;

import com.ledger.api.database.entities.PlayerBalance;

import java.util.ArrayList;
import java.util.List;

public class PlayerBalancesResponse {
    private List<PlayerBalance> balances;
    private long totalCount;
    private long pageSize;

    public List<PlayerBalance> getBalances() {
        if (balances == null) {
            balances = new ArrayList<>();
        }

        return balances;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setBalances(List<PlayerBalance> balances) {
        this.balances = balances;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
