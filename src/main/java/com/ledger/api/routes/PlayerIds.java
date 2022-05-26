package com.ledger.api.routes;

import com.ledger.api.database.entities.Player;
import com.ledger.api.database.repositories.PlayerRepository;
import com.ledger.api.dtos.PlayerId;
import com.ledger.api.services.SessionService;
import com.ledger.api.utils.HttpExchangeUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlayerIds implements HttpHandler {
    private final PlayerRepository repository;

    public PlayerIds(PlayerRepository repository) {
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

        try {
            SessionService.authorize(exchange);
        } catch (Exception e) {
            HttpExchangeUtils.unauthorized(exchange);
            return;
        }

        List<PlayerId> result = new ArrayList<>();
        for (Player playerBalance : repository.queryForAll()) {
            PlayerId dto = new PlayerId();
            dto.setId(playerBalance.getPlayerId());
            dto.setName(playerBalance.getPlayerName());
            result.add(dto);
        }

        result.sort(Comparator.comparing(PlayerId::getName));
        HttpExchangeUtils.success(exchange, result);
    }
}
