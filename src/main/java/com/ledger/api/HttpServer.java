package com.ledger.api;

import com.ledger.Ledger;
import com.ledger.api.routes.*;
import com.ledger.api.utils.ResourceRetriever;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.Executors;

/**
 * Starts an HTTP server which handles both
 * front-end (static assets) and back-end (API) requests
 */
public class HttpServer {
    private final com.sun.net.httpserver.HttpServer server;

    public HttpServer() throws IOException {
        int port = Ledger.getConfiguration().getInt("port");
        this.server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
    }

    /**
     * Creates routes and starts the server
     */
    public void start() throws IOException, URISyntaxException {
        this.createFrontEndRoutes();
        this.createRoutes();
        this.server.setExecutor(Executors.newCachedThreadPool());
        this.server.start();
    }

    public void stop() {
        this.server.stop(0);
    }

    /**
     * Creates front-end routes, for retrieving
     * html, css, javascript, and assets
     */
    private void createFrontEndRoutes() throws IOException, URISyntaxException {
        String host = Ledger.getConfiguration().getString("server-url");
        String url = "http://" + host + "/api";

        for (String path : ResourceRetriever.getResourcePaths("frontend")) {
            // Map / to index.html so 'index.html' is not required in the URL
            if ("frontend/index.html".equals(path)) {
                String contents = ResourceRetriever.getFileContents(path);
                server.createContext("/", new FileRoute(path, contents, null));
            } else if (path.endsWith(".js")) { // Back end URI needs to be dynamic
                String contents = ResourceRetriever.getFileContents(path);
                server.createContext(path.replace("frontend", ""), new FileRoute(path, contents.replace("{{{BACK_END_URI}}}", url), null));
            } else if (path.endsWith(".png") || path.endsWith(".ico")) {
                byte[] contents = ResourceRetriever.getFileBytes(path);
                server.createContext(path.replace("frontend", ""), new FileRoute(path, null, contents));
            } else {
                String contents = ResourceRetriever.getFileContents(path);
                server.createContext(path.replace("frontend", ""), new FileRoute(path, contents, null));
            }
        }
    }

    /**
     * Creates back-end routes
     */
    private void createRoutes() {
        server.createContext("/api/sessions", new Session());
        server.createContext("/api/player-ids", new PlayerIds());
        server.createContext("/api/transactions", new Transactions());
        server.createContext("/api/balances", new PlayerBalance());
        server.createContext("/api/players", new PlayerBalanceHistory());
        server.createContext("/api/server", new ServerBalanceHistory());
    }
}
