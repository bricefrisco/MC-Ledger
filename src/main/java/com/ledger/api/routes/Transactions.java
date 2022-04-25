package com.ledger.api.routes;

import com.ledger.Ledger;
import com.ledger.api.database.repositories.TransactionLogRepository;
import com.ledger.api.dtos.TransactionsResponse;
import com.ledger.api.dtos.Session;
import com.ledger.api.services.SessionService;
import com.ledger.api.utils.HttpExchangeUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class Transactions implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpExchangeUtils.setCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpExchangeUtils.preflight(exchange);
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpExchangeUtils.methodNotSupported(exchange);
            return;
        }

        Map<String, String> params = HttpExchangeUtils.parseQueryParams(exchange);
        String playerId = params.get("playerId");

        try {
            Session session = SessionService.authorize(exchange);
            if (session.getPlayerId().equals(playerId)) {
                if (!SessionService.hasPermission(exchange, "ledger.transactions.view-own") &&
                        !SessionService.hasPermission(exchange, "ledger.transactions.view-all")) {
                    HttpExchangeUtils.unauthorized(exchange);
                    return;
                }
            } else {
                if (!SessionService.hasPermission(exchange, "ledger.transactions.view-all")) {
                    HttpExchangeUtils.unauthorized(exchange);
                    return;
                }
            }
        } catch (Exception e) {
            HttpExchangeUtils.unauthorized(exchange);
            return;
        }

        int page = 0;
        try {
            String pageStr = params.get("page");
            if (pageStr != null && !pageStr.isBlank()) {
                page = Integer.parseInt(pageStr);
            }
        } catch (Exception e) {
            HttpExchangeUtils.badRequest(exchange, "Could not parse page parameter");
            return;
        }

        long timestamp = System.currentTimeMillis();
        try {
            String timestampStr = params.get("timestamp");
            if (timestampStr != null && !timestampStr.isBlank()) {
                timestamp = Long.parseLong(timestampStr);
            }
        } catch (Exception e) {
            HttpExchangeUtils.badRequest(exchange, "Could not parse timestamp parameter");
            return;
        }


        boolean ascending = "true".equalsIgnoreCase(params.get("ascending"));

        Ledger.getBukkitLogger().info("Player ID: '" + playerId + '"');
        TransactionsResponse response = TransactionLogRepository.query(playerId, page, ascending, timestamp);
        HttpExchangeUtils.success(exchange, response);
    }
}
