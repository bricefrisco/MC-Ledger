package com.ledger.api.database;
import com.ledger.Ledger;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.SqliteDatabaseType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.security.InvalidParameterException;
import java.sql.SQLException;

public class DaoCreator {
    /**
     * Returns a DAO for the given entity and with the given ID
     *
     * @param entity Entity's class
     * @param <ENTITY> Type of the entity
     * @param <ID> Type of the ID
     * @return Dao
     * @throws InvalidParameterException If the entity class is not annotated.
     * @throws SQLException If connection to the database cannot be established.
     */
    public static <ENTITY, ID> Dao<ENTITY, ID> getDao(Class<ENTITY> entity) throws InvalidParameterException, SQLException {
        if (!entity.isAnnotationPresent(DatabaseFileName.class)) {
            throw new InvalidParameterException("Entity not annotated with @DatabaseFileName!");
        }

        String fileName = entity.getAnnotation(DatabaseFileName.class).value();
        String uri = ConnectionManager.getURI(Ledger.loadFile(fileName));

        ConnectionSource connectionSource = new JdbcConnectionSource(uri, new SqliteDatabaseType());

        return DaoManager.createDao(connectionSource, entity);
    }

    /**
     * Creates a DAO as well as a default table, if it does not exist
     *
     * @see #getDao(Class)
     * @throws InvalidParameterException
     * @throws SQLException
     */
    public static <ENTITY, ID> Dao<ENTITY, ID> getDaoAndCreateTable(Class<ENTITY> entity) throws InvalidParameterException, SQLException {
        Dao<ENTITY, ID> dao = getDao(entity);

        TableUtils.createTableIfNotExists(dao.getConnectionSource(), entity);

        return dao;
    }

    /**
     * Truncates the table by dropping and recreating.
     *
     * @param entity Entity's class
     * @param <ENTITY> Type of the entity
     * @param <ID> Type of the ID
     * @throws InvalidParameterException
     * @throws SQLException
     */
    public static <ENTITY, ID> void truncateTable(Class<ENTITY> entity) throws InvalidParameterException, SQLException {
        Dao<ENTITY, ID> dao = getDao(entity);
        TableUtils.dropTable(dao.getConnectionSource(), entity, false);
        TableUtils.createTableIfNotExists(dao.getConnectionSource(), entity);
    }
}