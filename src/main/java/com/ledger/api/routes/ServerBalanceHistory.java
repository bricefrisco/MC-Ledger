package com.ledger.api.routes;

import com.ledger.api.database.repositories.ServerBalanceHistoryRepository;
import com.ledger.api.services.SessionService;
import com.ledger.api.utils.HttpExchangeUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ServerBalanceHistory implements HttpHandler {
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

        if (!SessionService.hasPermission(exchange, "ledger.server-chart.view")) {
            HttpExchangeUtils.unauthorized(exchange);
            return;
        }

        Map<String, String> params = HttpExchangeUtils.parseQueryParams(exchange);

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
        for (com.ledger.api.database.entities.ServerBalanceHistory history : ServerBalanceHistoryRepository.query(month)) {
            histories.add(history.getTimestamp());
            histories.add(history.getNumberOfPlayersTracked());
            histories.add(history.getBalance());
        }

        HttpExchangeUtils.success(exchange, histories);
    }
}
