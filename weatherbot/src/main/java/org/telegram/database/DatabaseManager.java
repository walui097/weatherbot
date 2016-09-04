package org.telegram.database;

import org.telegram.telegrambots.logging.BotLogger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseManager {
    private static final String LOGTAG = "DATABASEMANAGER";

    private static volatile DatabaseManager instance;
    private static volatile ConnectionDB connection;

    /**
     * Private constructor (due to Singleton)
     */
    private DatabaseManager() {
    	connection = new ConnectionDB();
    }

    /**
     * Get Singleton instance
     *
     * @return instance of the class
     */
    public static DatabaseManager getInstance() {
        final DatabaseManager currentInstance;
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
                currentInstance = instance;
            }
        } else {
            currentInstance = instance;
        }
        return currentInstance;
    }
    
    public class WeatherSubscribe {
        private int userId;
        private boolean subscribeWarning = false;
        private boolean subscribeCurrentWeather = false;

        public WeatherSubscribe() {
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public boolean getSubscribeWarning() {
            return subscribeWarning;
        }

        public void setSubscribeWarning(boolean subscribeWarning) {
            this.subscribeWarning = subscribeWarning;
        }

        public boolean getSubscribeCurrentWeather() {
            return subscribeCurrentWeather;
        }

        public void setSubscribeCurrentWeather(boolean subscribeCurrentWeather) {
            this.subscribeCurrentWeather = subscribeCurrentWeather;
        }
    }
    
    
    public List<WeatherSubscribe> getAllSubscribes() {
        List<WeatherSubscribe> allSubscribes = new ArrayList<>();

        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement("select * FROM WeatherSubscribe");
            final ResultSet result = preparedStatement.executeQuery();
            
            while (result.next()) {
            	WeatherSubscribe weatherSubscribe = new WeatherSubscribe();
            	weatherSubscribe.setUserId(result.getInt("userId"));
            	weatherSubscribe.setSubscribeWarning(result.getBoolean("subscribeWarning"));
            	weatherSubscribe.setSubscribeCurrentWeather(result.getBoolean("subscribeCurrentWeather"));
            	allSubscribes.add(weatherSubscribe);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allSubscribes;
    }
    
    public boolean setCurrentWeatherSubscribe(Integer userId, boolean isSubscribeCurrentWeather) {
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement
            		("INSERT INTO WeatherSubscribe (userId, subscribeCurrentWeather) "
            				+ "VALUES(?, ?) ON DUPLICATE KEY UPDATE subscribeCurrentWeather=?");
            preparedStatement.setInt(1, userId);
            preparedStatement.setBoolean(2, isSubscribeCurrentWeather);
            preparedStatement.setBoolean(3, isSubscribeCurrentWeather);

            updatedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedRows > 0;
    }
    
    public boolean setWarningSubscribe(Integer userId, boolean isSubscribeWarning) {
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement
            		("INSERT INTO WeatherSubscribe (userId, subscribeWarning) "
            				+ "VALUES(?, ?) ON DUPLICATE KEY UPDATE subscribeWarning=?");
            preparedStatement.setInt(1, userId);
            preparedStatement.setBoolean(2, isSubscribeWarning);
            preparedStatement.setBoolean(3, isSubscribeWarning);

            updatedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedRows > 0;
    }

    public boolean getSubscribeCurrentWeather(Integer userId) {
        boolean isSubscribeCurrentWeather = false;
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement
            		("Select subscribeCurrentWeather FROM WeatherSubscribe WHERE userId=?");
            preparedStatement.setInt(1, userId);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
            	isSubscribeCurrentWeather = result.getBoolean("subscribeCurrentWeather");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isSubscribeCurrentWeather;
    }
    
    public boolean getSubscribeWarning(Integer userId) {
        boolean isSubscribeWarning = false;
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement
            		("Select subscribeWarning FROM WeatherSubscribe WHERE userId=?");
            preparedStatement.setInt(1, userId);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
            	isSubscribeWarning = result.getBoolean("subscribeWarning");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isSubscribeWarning;
    }
    
    public boolean setUserState(Integer userId, int status) {
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement
            		("INSERT INTO UserTable (userId, status) VALUES(?, ?) ON DUPLICATE KEY UPDATE status=?");
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, status);
            preparedStatement.setInt(3, status);

            updatedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedRows > 0;
    }

    public int getUserState(Integer userId) {
        int status = 0;
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement
            		("Select status FROM UserTable WHERE userId=?");
            preparedStatement.setInt(1, userId);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
                status = result.getInt("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return status;
    }
    
    public boolean setLanguage(Integer userId, String languageCode) {
        int updatedRows = 0;
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement
            		("INSERT INTO UserLanguage (userId, languageCode) VALUES(?, ?) ON DUPLICATE KEY UPDATE languageCode=?");
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, languageCode);
            preparedStatement.setString(3, languageCode);

            updatedRows = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updatedRows > 0;
    }

    public String getLanguage(Integer userId) {
        String languageCode = "en";
        try {
            final PreparedStatement preparedStatement = connection.getPreparedStatement
            		("Select languageCode FROM UserLanguage WHERE userId=?");
            preparedStatement.setInt(1, userId);
            final ResultSet result = preparedStatement.executeQuery();
            if (result.next()) {
            	languageCode = result.getString("languageCode");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return languageCode;
    }

}