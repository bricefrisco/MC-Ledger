package com.ledger.api.routes;

import com.ledger.api.database.repositories.PlayerBalanceHistoryRepository;
import com.ledger.api.dtos.Session;
import com.ledger.api.services.SessionService;
import com.ledger.api.utils.HttpExchangeUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class PlayerBalanceHistory implements HttpHandler {
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

        String uuid = params.get("uuid");
        if (uuid == null) {
            HttpExchangeUtils.badRequest(exchange, "Parameter uuid required");
            return;
        }

        try {
            Session session = SessionService.authorize(exchange);
            if (session.getPlayerId().equals(uuid)) {
                if (!SessionService.hasPermission(exchange, "ledger.player-charts.view-own") &&
                        !SessionService.hasPermission(exchange, "ledger.player-charts.view-all")) {
                    HttpExchangeUtils.unauthorized(exchange);
                    return;
                }
            } else {
                if (!SessionService.hasPermission(exchange, "ledger.player-charts.view-all")) {
                    HttpExchangeUtils.unauthorized(exchange);
                    return;
                }
            }
        } catch (Exception e) {
            HttpExchangeUtils.unauthorized(exchange);
            return;
        }

        Integer month = null;
        try {
            String pageStr = params.get("month");
            if (pageStr != null && !pageStr.isBlank()) {
                month = Integer.parseInt(pageStr);
            }
        } catch (Exception e) {
            HttpExchangeUtils.badRequest(exchange, "Could not parse page parameter");
            return;
        }

        if (month == null || month < 1 || month > 12) {
            HttpExchangeUtils.badRequest(exchange, "Parameter month required between values 1 and 12");
            return;
        }

        ArrayList<Object> histories = new ArrayList<>();
        for (com.ledger.api.database.entities.PlayerBalanceHistory history : PlayerBalanceHistoryRepository.query(uuid, month)) {
            histories.add(history.getTimestamp());
            histories.add(history.getBalance());
        }

        HttpExchangeUtils.success(exchange, histories);
    }
}
