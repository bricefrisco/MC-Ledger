package com.ledger.api.routes;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * Handles file requests
 * Index.html, javascript, css files, images, and other static assets.
 */
public class FileRoute implements HttpHandler {
    private final String contentType;
    private final String contents;
    private final byte[] byteContents;

    /**
     * @param fileName the file name to be served
     * @param contents the contents of the file
     */
    public FileRoute(String fileName, String contents, byte[] byteContents) {
        this.contentType = URLConnection.guessContentTypeFromName(fileName);
        this.contents = contents;
        this.byteContents = byteContents;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (this.contentType != null) {
            t.getResponseHeaders().set("Content-Type", this.contentType);
        }

        t.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
        t.getResponseHeaders().set("Access-Control-Allow-Methods", "*");
        t.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        if (this.contents != null) {
            t.sendResponseHeaders(200, this.contents.getBytes(StandardCharsets.UTF_8).length);
            t.getResponseBody().write(this.contents.getBytes(StandardCharsets.UTF_8));
        } else {
            t.sendResponseHeaders(200, this.byteContents.length);
            t.getResponseBody().write(this.byteContents);
        }

        t.getResponseBody().close();
    }
}
