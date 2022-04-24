package com.ledger.api.routes;

import com.ledger.api.services.SessionService;
import com.ledger.api.utils.HttpExchangeUtils;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Session implements HttpHandler {
    private static final Gson GSON = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpExchangeUtils.setCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpExchangeUtils.preflight(exchange);
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            HttpExchangeUtils.methodNotSupported(exchange);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        com.ledger.api.dtos.Session auth = GSON.fromJson(body, com.ledger.api.dtos.Session.class);

        com.ledger.api.dtos.Session session;
        try {
            session = SessionService.createSession(auth.getId());
        } catch (Exception e) {
            HttpExchangeUtils.unauthorized(exchange);
            return;
        }

        HttpExchangeUtils.success(exchange, session);
    }
}
