package com.ledger.api.routes;
import com.ledger.api.database.entities.PlayerBalance;
import com.ledger.api.database.repositories.PlayerBalanceRepository;
import com.ledger.api.dtos.PlayerId;
import com.ledger.api.services.SessionService;
import com.ledger.api.utils.HttpExchangeUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerIds implements HttpHandler {
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

        try {
            SessionService.authorize(exchange);
        } catch (Exception e) {
            HttpExchangeUtils.unauthorized(exchange);
            return;
        }

        List<PlayerId> result = new ArrayList<>();
        for (PlayerBalance playerBalance : PlayerBalanceRepository.queryForAll()) {
            PlayerId dto = new PlayerId();
            dto.setId(playerBalance.getPlayerId());
            dto.setName(playerBalance.getPlayerName());
            result.add(dto);
        }

        HttpExchangeUtils.success(exchange, result);
    }
}
