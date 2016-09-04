/*
 * This is the source code of Telegram Bot v. 2.0
 * It is licensed under GNU GPL v. 3 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Ruben Bermudez, 3/12/14.
 */
package org.telegram.database;

import org.telegram.BotConfig;
import org.telegram.telegrambots.logging.BotLogger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionDB {
    private static final String LOGTAG = "CONNECTIONDB";
    private Connection currentConnection;

    public ConnectionDB() {
        this.currentConnection = openConnection();
    }

    private Connection openConnection() {
        Connection connection = null;
        try {
            Class.forName(BotConfig.controllerDB).newInstance();
            connection = DriverManager.getConnection(BotConfig.linkDB, BotConfig.userDB, BotConfig.password);
        } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            BotLogger.error(LOGTAG, e);
        }

        return connection;
    }

    public void closeConnecion() {
        try {
            this.currentConnection.close();
        } catch (SQLException e) {
            BotLogger.error(LOGTAG, e);
        }

    }

    public ResultSet runSqlQuery(String query) throws SQLException {
        final Statement statement;
        statement = this.currentConnection.createStatement();
        return statement.executeQuery(query);
    }

    public Boolean executeQuery(String query) throws SQLException {
        final Statement statement = this.currentConnection.createStatement();
        return statement.execute(query);
    }

    public PreparedStatement getPreparedStatement(String query) throws SQLException {
        return this.currentConnection.prepareStatement(query);
    }

    public PreparedStatement getPreparedStatement(String query, int flags) throws SQLException {
        return this.currentConnection.prepareStatement(query, flags);
    }

}
