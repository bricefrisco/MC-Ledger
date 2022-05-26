package com.ledger.api.routes;

import com.ledger.api.database.entities.HistoryType;
import com.ledger.api.database.repositories.PlayerBalanceRepository;
import com.ledger.api.database.repositories.PlayerRepository;
import com.ledger.api.database.repositories.TransactionRepository;
import com.ledger.api.dtos.PlayerBalanceResponse;
import com.ledger.api.dtos.Session;
import com.ledger.api.services.SessionService;
import com.ledger.api.utils.HttpExchangeUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

public class PlayerBalance implements HttpHandler {
    private final PlayerBalanceRepository playerBalanceRepository;
    private final PlayerRepository playerRepository;
    private final TransactionRepository transactionRepository;

    public PlayerBalance(PlayerBalanceRepository playerBalanceRepository, PlayerRepository playerRepository, TransactionRepository transactionRepository) {
        this.playerBalanceRepository = playerBalanceRepository;
        this.playerRepository = playerRepository;
        this.transactionRepository = transactionRepository;
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

        ArrayList<Object> data = new ArrayList<>();
        for (com.ledger.api.database.entities.PlayerBalance balance : playerBalanceRepository.query(uuid, historyType)) {
            data.add(balance.getTimestamp());
            data.add(balance.getBalance());
        }

        PlayerBalanceResponse response = new PlayerBalanceResponse();
        response.setData(data);

        if (historyType == HistoryType.ALL_TIME) {
            response.setStart(transactionRepository.queryFirstBalance(uuid));
        } else {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            switch (historyType) {
                case DAILY -> cal.add(Calendar.DAY_OF_YEAR, -1);
                case WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, -7);
                case MONTHLY -> cal.add(Calendar.MONTH, -1);
            }
            long timestamp = cal.getTimeInMillis();
            response.setStart(transactionRepository.queryBalanceAt(uuid, timestamp));
        }

        response.setCurrent(playerRepository.getCurrentBalance(uuid));
        HttpExchangeUtils.success(exchange, response);
    }
}
