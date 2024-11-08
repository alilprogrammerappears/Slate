package com.slate.sys;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConnectionPool {
    public static final int INITIAL_POOL_SIZE = 5;

    private final String url;
    private final String username;
    private final String password;
    private final int maxPoolSize;
    private final List<Connection> connections;
    private final List<Connection> usedConnections = new ArrayList<>();

    private ConnectionPool (String url, String username, String password, int maxPoolSize, List<Connection> connections) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.connections = connections;
    }
    public static ConnectionPool create (String url, String username, String password, int maxPoolSize)
        throws SQLException {
        maxPoolSize = Math.max(maxPoolSize, INITIAL_POOL_SIZE);

        List<Connection> pool = new ArrayList<>(maxPoolSize);
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            pool.add(DriverManager.getConnection(url, username, password));
        }
        return new ConnectionPool(url, username, password, maxPoolSize, pool);
    }
    public static ConnectionPool createEmpty () {
        return new ConnectionPool("", "", "", 0, List.of());
    }

    private Connection createConnection () throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public void release (Connection connection) {
        if (usedConnections.remove(connection)) {
            connections.add(connection);
        }
    }
    private void releaseAll () {
        connections.addAll(usedConnections);
        usedConnections.clear();
    }

    public void close () throws SQLException {
        releaseAll();

        for (Connection connection : connections) {
            connection.close();
        }
        connections.clear();
    }

    public Optional<Connection> getConnection () {
        if (connections.isEmpty() && usedConnections.size() < maxPoolSize) {
            try {
                connections.add(createConnection());
            }
            catch (Exception e) {
                return Optional.empty();
            }
        }

        Connection connection = connections.remove(0);
        try {
            if (!connection.isValid(3)) {
                connection = createConnection();
            }
        }
        catch (Exception ignored) {}
        return Optional.ofNullable(connection);
    }

    public int size () {
        return connections.size() + usedConnections.size();
    }
}
