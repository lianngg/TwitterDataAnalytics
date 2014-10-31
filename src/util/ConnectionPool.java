package util;

import java.sql.*;
import java.util.Enumeration;
import java.util.Vector;
import com.mysql.jdbc.Driver;

public class ConnectionPool {

    private String jdbcDriver = "";
    private String dbUrl = "";
    private String dbUsername = "";
    private String dbPassword = "";
    private int loginTimeout = 120;
    private String testTable = "";
    private int initialConnections = 10;
    private int incrementalConnections = 5;
    private int maxConnections = 200;
    private Vector connections = null;

    public ConnectionPool(String jdbcDriver, String dbUrl, String dbUsername, String dbPassword, int loginTimeout) {
        this.jdbcDriver = jdbcDriver;
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.loginTimeout = loginTimeout;
    }

    public synchronized void createPool() throws Exception {
        if (connections != null) {
            return;
        }
        Driver driver = (Driver) (Class.forName(this.jdbcDriver).newInstance());
        DriverManager.registerDriver(driver);
        connections = new Vector();
        createConnections(this.initialConnections);
       // log.info("Connection Pool created successfully.");
    }

    public synchronized void closeConnectionPool() throws SQLException {
        if (connections == null) {
            return;
        }

        PooledConnection pConn = null;
        Enumeration em = connections.elements();
        while (em.hasMoreElements()) {
            pConn = (PooledConnection) em.nextElement();
            if (pConn.isBusy()) {
                wait(5000);
            }
            closeConnection(pConn.getConnection());
            connections.removeElement(pConn);
        }
        connections = null;
    }

    public synchronized Connection getConnection() throws Exception {
        if (connections == null) {
            throw new NullPointerException("connections is null");
        }
        Connection conn = getFreeConnection();
        while (conn == null) {
            wait(100);
            conn = getFreeConnection();
        }
        return conn;
    }

    public boolean returnConnection(Connection conn) {
        if (connections == null) {
            return true;
        }
        PooledConnection pConn = null;
        Enumeration em = connections.elements();
        while (em.hasMoreElements()) {
            pConn = (PooledConnection) em.nextElement();
            if (conn == pConn.getConnection()) {
                pConn.setBusy(false);
                return true;
            }
        }
        return false;
    }

    public synchronized void refreshConnections() throws SQLException {
        if (connections == null) {
            return;
        }
        PooledConnection pConn = null;
        Enumeration em = connections.elements();
        while (em.hasMoreElements()) {
            pConn = (PooledConnection) em.nextElement();
            if (pConn.isBusy()) {
                wait(1000);
            }
            closeConnection(pConn.getConnection());
            pConn.setConnection(newConnection());
            pConn.setBusy(false);
        }
    }

    public int getInitialConnections() {
        return this.initialConnections;
    }

    public void setInitialConnections(int initialConnections) {
        this.initialConnections = initialConnections;
    }

    public int getIncrementalConnections() {
        return this.incrementalConnections;
    }

    public void setIncrementalConnections(int incrementalConnections) {
        this.incrementalConnections = incrementalConnections;
    }

    public int getMaxConnections() {
        return this.maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getTestTable() {
        return this.testTable;
    }

    public void setTestTable(String testTable) {
        this.testTable = testTable;
    }

    private void createConnections(int numConnections) throws SQLException {
        for (int x = 0; x < numConnections; x++) {
            if (this.maxConnections > 0 && this.connections.size() >= this.maxConnections) {
                break;
            }
            try {
                connections.addElement(new PooledConnection(newConnection()));
            } catch (SQLException e) {
                System.out.print(e.getMessage());
                // throw new SQLException();

            }
           // log.info("Database connection created.");
        }
    }

    private Connection newConnection() throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
        Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        conn.setAutoCommit(true);

        if (connections.size() == 0) {
            DatabaseMetaData metaData = conn.getMetaData();
            int driverMaxConnections = metaData.getMaxConnections();
            if (driverMaxConnections > 0 && this.maxConnections > driverMaxConnections) {
                this.maxConnections = driverMaxConnections;
            }
        }
        return conn;
    }

    private Connection getFreeConnection() throws SQLException {
        Connection conn = findFreeConnection();
        if (conn == null) {
            createConnections(incrementalConnections);
            conn = findFreeConnection();
            if (conn == null) {
                return null;
            }
        }
        return conn;
    }

    private Connection findFreeConnection() throws SQLException {
        Connection conn = null;
        PooledConnection pConn = null;
        Enumeration em = connections.elements();

        while (em.hasMoreElements()) {
            pConn = (PooledConnection) em.nextElement();
            if (!pConn.isBusy()) {
                conn = pConn.getConnection();
                pConn.setBusy(true);
                if (!testConnection(conn)) {
                    try {
                        conn = newConnection();
                    } catch (SQLException e) {
                        return null;
                    }
                    pConn.setConnection(conn);
                }
                break;
            }
        }
        return conn;
    }

    private boolean testConnection(Connection conn) {
        try {
            if (testTable.equals("")) {
                conn.setAutoCommit(true);
            } else {
                Statement stmt = conn.createStatement();
                stmt.execute("select count(*) from " + testTable);
                stmt.close();
            }
        } catch (SQLException e) {
            closeConnection(conn);
            return false;
        }
        return true;
    }

    private void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
        }
    }

    private void wait(int mSeconds) {
        try {
            Thread.sleep(mSeconds);
        } catch (InterruptedException e) {
        }
    }

    class PooledConnection {
        Connection connection = null;
        boolean busy = false;

        public PooledConnection(Connection connection) {
            this.connection = connection;
        }

        public Connection getConnection() {
            return connection;
        }

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        public boolean isBusy() {
            return busy;
        }

        public void setBusy(boolean busy) {
            this.busy = busy;
        }
    }

}
