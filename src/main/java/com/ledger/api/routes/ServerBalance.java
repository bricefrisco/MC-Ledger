package com.ledger.api.routes;

import com.ledger.api.database.entities.HistoryType;
import com.ledger.api.database.repositories.ServerBalanceRepository;
import com.ledger.api.services.SessionService;
import com.ledger.api.utils.HttpExchangeUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ServerBalance implements HttpHandler {
    private final ServerBalanceRepository repository;

    public ServerBalance(ServerBalanceRepository repository) {
        this.repository = repository;
    }

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

        String historyTypeStr = params.get("historyType");
        if (historyTypeStr == null) {
            historyTypeStr = "daily";
        }

        HistoryType historyType;
        try {
            historyType = HistoryType.valueOf(historyTypeStr.toUpperCase());
        } catch (Exception e) {
            HttpExchangeUtils.badRequest(exchange, "Could not parse history type '" + historyTypeStr + "'");
            return;
        }

        ArrayList<Object> history = new ArrayList<>();
        for (com.ledger.api.database.entities.ServerBalance balance : repository.query(historyType)) {
            history.add(balance.getTimestamp());
            history.add(balance.getNumberOfPlayersTracked());
            history.add(balance.getBalance());
        }

        HttpExchangeUtils.success(exchange, history);
    }
}
