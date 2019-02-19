package io.github.bennyboy1695.shadowtrades.Storage;

import io.github.bennyboy1695.shadowtrades.ShadowTrades;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite extends Database {
    private final String dbLocation;
    private final File dataFolder;
    private Connection conn;

    public SQLite(File dbFile, File dataFolder) {
        this.dataFolder = dataFolder;
        this.dbLocation = dbFile.getAbsolutePath();
    }

    @Override
    public Connection openConnection() throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            return this.conn;
        }
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File dbFile = new File(this.dbLocation);
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                //TODO Gen crash report
                ShadowTrades.getInstance().getLogger().warn("Unable to create database. See above for details.");
                System.out.println("Unable to create db file");
            }
        }
        Class.forName("org.sqlite.JDBC");
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbLocation);
        return this.conn;
    }

    @Override
    public boolean checkConnection() throws SQLException {
        return (this.conn != null) && !this.conn.isClosed();
    }

    @Override
    public Connection getConnection() {
        return this.conn;
    }

    @Override
    public boolean closeConnection() throws SQLException {
        if (this.conn == null) {
            return false;
        }
        this.conn.close();
        this.conn = null;
        return true;
    }

    @Override
    public int updateSQL(String query) throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            openConnection();
        }
        try (Statement stmt = this.conn.createStatement()) {
            return stmt.executeUpdate(query);
        }
    }
}
