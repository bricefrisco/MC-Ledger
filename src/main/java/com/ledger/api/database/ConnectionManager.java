package com.ledger.api.database;

import java.io.File;

public class ConnectionManager {
    private static final String URI_STRING = "jdbc:sqlite:%s";

    public static String getURI(File databaseFile) {
        return String.format(URI_STRING, databaseFile.getAbsolutePath());
    }
}
