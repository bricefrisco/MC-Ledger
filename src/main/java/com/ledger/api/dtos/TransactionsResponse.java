package com.ledger.api.dtos;

import com.ledger.api.database.entities.Transaction;
import java.util.ArrayList;
import java.util.List;

public class TransactionsResponse {
    private List<Transaction> transactions;
    private long totalCount;
    private long pageSize;

    public List<Transaction> getTransactions() {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        return transactions;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
