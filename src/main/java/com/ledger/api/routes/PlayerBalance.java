package com.ledger.api.routes;

import com.ledger.api.database.repositories.PlayerBalanceRepository;
import com.ledger.api.dtos.PlayerBalancesResponse;
import com.ledger.api.services.SessionService;
import com.ledger.api.utils.HttpExchangeUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class PlayerBalance implements HttpHandler {
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

        PlayerBalancesResponse balances = PlayerBalanceRepository.query(page);
        HttpExchangeUtils.success(exchange, balances);
    }
}
