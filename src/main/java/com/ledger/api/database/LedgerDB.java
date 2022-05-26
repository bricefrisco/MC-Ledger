package com.ledger.api.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.SqliteDatabaseType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class LedgerDB {
    private static final String DATABASE_NAME = "Ledger.db";
    private static final String URI_STRING = "jdbc:sqlite:%s";
    private final File databaseFile;

    public LedgerDB(File dataFolder) throws IOException {
        File databaseFile = new File(dataFolder, DATABASE_NAME);
        databaseFile.createNewFile();
        this.databaseFile = databaseFile;
    }

    public <E, I> Dao<E, I> getDao(Class<E> entity) throws SQLException {
        ConnectionSource connectionSource = new JdbcConnectionSource(
                String.format(URI_STRING, databaseFile.getAbsolutePath()),
                new SqliteDatabaseType()
        );

        Dao<E, I> dao = DaoManager.createDao(connectionSource, entity);
        TableUtils.createTableIfNotExists(dao.getConnectionSource(), entity);
        return dao;
    }
}
