package com.ledger.api.routes;

import com.ledger.api.database.repositories.PlayerRepository;
import com.ledger.api.dtos.PlayersResponse;
import com.ledger.api.services.SessionService;
import com.ledger.api.utils.HttpExchangeUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class Player implements HttpHandler {
    private final PlayerRepository repository;

    public Player(PlayerRepository repository) {
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

        if (!SessionService.hasPermission(exchange, "ledger.balances.view")) {
            HttpExchangeUtils.unauthorized(exchange);
            return;
        }

        Map<String, String> params = HttpExchangeUtils.parseQueryParams(exchange);

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

        PlayersResponse balances = repository.query(page);
        HttpExchangeUtils.success(exchange, balances);
    }
}
