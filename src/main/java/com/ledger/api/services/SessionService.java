package com.ledger.api.services;

import com.ledger.api.dtos.Session;
import com.sun.net.httpserver.HttpExchange;

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.CredentialNotFoundException;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SessionService {
    private static final ConcurrentHashMap<String, Session> AUTHORIZATIONS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();

    public static String createAuthorization(String playerId, String playerName, List<String> permissions) {
        String id = UUID.randomUUID().toString().replaceAll("-", "");
        Session authorization = new Session();
        authorization.setId(id);
        authorization.setPlayerId(playerId);
        authorization.setPlayerName(playerName);
        authorization.setPermissions(permissions);
        authorization.setExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2)));
        AUTHORIZATIONS.put(id, authorization);
        return id;
    }

    public static Session createSession(String authId) throws CredentialException {
        Session authorization = AUTHORIZATIONS.get(authId);

        if (authorization == null) {
            throw new CredentialException("Authorization id not found or expired.");
        }

        if (new Date(System.currentTimeMillis()).getTime() > authorization.getExpiresAt().getTime()) {
            AUTHORIZATIONS.remove(authorization.getId());
            throw new CredentialExpiredException("Authorization id not found or expired.");
        }

        AUTHORIZATIONS.remove(authorization.getId());
        String id = UUID.randomUUID().toString().replaceAll("-", "");

        Session session = new Session();
        session.setId(id);
        session.setPlayerId(authorization.getPlayerId());
        session.setPlayerName(authorization.getPlayerName());
        session.setPermissions(authorization.getPermissions());
        session.setExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));
        SESSIONS.put(id, session);

        return session;
    }

    public static void purgeOldAuthorizations() {
        long now = new Date(System.currentTimeMillis()).getTime();
        for (Map.Entry<String, Session> entry : AUTHORIZATIONS.entrySet()) {
            Session session = entry.getValue();
            if (now > session.getExpiresAt().getTime()) {
                AUTHORIZATIONS.remove(entry.getKey());
            }
        }
    }

    public static void purgeOldSessions() {
        long now = new Date(System.currentTimeMillis()).getTime();
        for (Map.Entry<String, Session> entry : SESSIONS.entrySet()) {
            Session session = entry.getValue();
            if (now > session.getExpiresAt().getTime()) {
                SESSIONS.remove(entry.getKey());
            }
        }
    }

    public static Session authorize(HttpExchange exchange) throws CredentialNotFoundException, CredentialExpiredException {
        if (exchange.getRequestHeaders() == null) {
            throw new CredentialNotFoundException("Unauthorized");
        }

        if (exchange.getRequestHeaders().get("Authorization") == null || exchange.getRequestHeaders().get("Authorization").size() == 0) {
            throw new CredentialNotFoundException("Unauthorized");
        }

        String sessionId = exchange.getRequestHeaders().get("Authorization").get(0);

        if (sessionId == null) {
            throw new CredentialNotFoundException("Session not found");
        }

        Session session = SESSIONS.get(sessionId);
        if (session == null) {
            throw new CredentialNotFoundException("Session not found");
        }

        if (new Date(System.currentTimeMillis()).getTime() > session.getExpiresAt().getTime()) {
            SESSIONS.remove(sessionId);
            throw new CredentialExpiredException("Session expired");
        }

        renewSession(sessionId);
        return session;
    }

    public static boolean hasPermission(HttpExchange exchange, String permission) {
        try {
            Session session = authorize(exchange);
            return session.getPermissions().contains(permission);
        } catch (Exception e) {
            return false;
        }
    }

    private static void renewSession(String id) {
        Session session = SESSIONS.get(id);
        if (session == null) {
            return;
        }
        session.setExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));
    }
}
