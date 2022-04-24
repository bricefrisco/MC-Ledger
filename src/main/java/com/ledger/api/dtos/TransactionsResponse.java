package com.ledger.api.dtos;

import com.ledger.api.database.entities.TransactionLog;

import java.util.ArrayList;
import java.util.List;

public class TransactionsResponse {
    private List<TransactionLog> transactions;
    private long totalCount;
    private long pageSize;

    public List<TransactionLog> getTransactions() {
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

    public void setTransactions(List<TransactionLog> transactions) {
        this.transactions = transactions;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
