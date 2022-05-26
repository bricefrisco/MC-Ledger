package com.ledger.api.utils;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpExchangeUtils {
    private static final Gson GSON = new Gson();

    public static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
        exchange.getResponseHeaders().set("Content-Type", "application/json");
    }

    public static void methodNotSupported(HttpExchange exchange) throws IOException {
        String response = GSON.toJson("Method not supported");
        exchange.sendResponseHeaders(405, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.getResponseBody().close();
    }

    public static void preflight(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, 0);
        exchange.getResponseBody().close();
    }

    public static void unauthorized(HttpExchange exchange) throws IOException {
        String response = GSON.toJson("Unauthorized");
        exchange.sendResponseHeaders(401, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.getResponseBody().close();
    }

    public static void success(HttpExchange exchange, Object body) throws IOException {
        String response = GSON.toJson(body);
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.getResponseBody().close();
    }

    public static void badRequest(HttpExchange exchange, String message) throws IOException {
        String response = GSON.toJson(message);
        exchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        exchange.getResponseBody().close();
    }

    public static String getAuthorization(HttpExchange exchange) {
        return exchange.getRequestHeaders().getFirst("Authorization");
    }

    public static Map<String, String> parseQueryParams(HttpExchange exchange) {
        Map<String, String> result = new HashMap<>();
        if (exchange == null || exchange.getRequestURI().getRawQuery() == null || exchange.getRequestURI().getRawQuery().isBlank()) {
            return result;
        }

        String params = exchange.getRequestURI().getRawQuery();
        for (String param : params.split("&")) {
            param = URLDecoder.decode(param, StandardCharsets.UTF_8);
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }

        return result;
    }
}
