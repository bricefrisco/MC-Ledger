package com.ledger.api.dtos;

import com.ledger.api.database.entities.Player;
import java.util.ArrayList;
import java.util.List;

public class PlayersResponse {
    private List<Player> balances;
    private long totalCount;
    private long pageSize;

    public List<Player> getBalances() {
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

    public void setBalances(List<Player> balances) {
        this.balances = balances;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
