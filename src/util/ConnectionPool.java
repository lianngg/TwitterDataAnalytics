package util;

import java.sql.*;
import java.util.Enumeration;
import java.util.Vector;
import com.mysql.jdbc.Driver;

public class ConnectionPool {


    // 資料庫驅動
    private String jdbcDriver = "";
    // 數據 URL
    private String dbUrl = "";
    // 資料庫用戶名
    private String dbUsername = "";
    // 資料庫使用者密碼
    private String dbPassword = "";
    // 資料庫登錄超時（秒）
    private int loginTimeout = 120;
    // 測試連接是否可用的測試表名，默認沒有測試表
    private String testTable = "";
    // 連接池的初始大小
    private int initialConnections = 10;
    // 連接池自動增加的大小
    private int incrementalConnections = 5;
    // 連接池最大的大小
    private int maxConnections = 200;
    // 存放連接池中資料庫連接的向量 , 初始時為 null, 它中存放的物件為 PooledConnection 型
    private Vector connections = null;

    /**
     * 構造函數
     *
     * @param jdbcDriver   String JDBC 驅動類串
     * @param dbUrl        String 資料庫 URL
     * @param dbUsername   String 連接資料庫用戶名
     * @param dbPassword   String 連接資料庫使用者的密碼
     * @param loginTimeout int 資料庫登錄超時(秒)
     */
    public ConnectionPool(String jdbcDriver, String dbUrl, String dbUsername, String dbPassword, int loginTimeout) {
        this.jdbcDriver = jdbcDriver;
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.loginTimeout = loginTimeout;
    }

    /**
     * 創建一個資料庫連接池，連接池中的可用連接的數量採用類成員
     * initialConnections 中設置的值
     */

    public synchronized void createPool() throws Exception {
        if (connections != null) {
            return;
        }
        // 產生實體 JDBC Driver 中指定的驅動類實例
        Driver driver = (Driver) (Class.forName(this.jdbcDriver).newInstance());
        DriverManager.registerDriver(driver); // 註冊 JDBC 驅動程式
        // 創建保存連接的向量 , 初始時有 0 個元素
        connections = new Vector();
        // 根據 initialConnections 中設置的值，創建連接。
        createConnections(this.initialConnections);
       // log.info("資料庫連接池創建成功!");
    }

    /**
     * 關閉連接池中所有的連接，並清空連接池。
     */
    public synchronized void closeConnectionPool() throws SQLException {
        if (connections == null) {
           // log.error("連接池不存在,無法關閉!");
            return;
        }

        PooledConnection pConn = null;
        Enumeration em = connections.elements();
        while (em.hasMoreElements()) {
            pConn = (PooledConnection) em.nextElement();
            // 如果忙，等5秒,5 秒後直接關閉它
            if (pConn.isBusy()) {
                wait(5000);
            }
            closeConnection(pConn.getConnection());
            // 從連接池向量中刪除它
            connections.removeElement(pConn);
        }
        connections = null;
        //log.info("所有資料庫連接已斷開,連接池成功關閉!");
    }

    /**
     * 通過調用 getFreeConnection() 函數返回一個可用的資料庫連接 ,
     * 如果當前沒有可用的資料庫連接，並且更多的資料庫連接不能創
     * 建（如連接池大小的限制），此函數等待一會再嘗試獲取。
     *
     * @return 返回一個可用的資料庫連線物件
     */
    public synchronized Connection getConnection() throws Exception {
        if (connections == null) {
            throw new NullPointerException("連接池還沒創建，無法獲取連接");
        }

        Connection conn = getFreeConnection(); // 獲得一個可用的資料庫連接
        // 如果目前沒有可以使用的連接，即所有的連接都在使用中
        while (conn == null) {
            wait(250);
            conn = getFreeConnection(); // 重新再試，直到獲得可用的連接，否則為忙,等待
        }

        return conn;
    }


    /**
     * 此函數返回一個資料庫連接到連接池中，並把此連接置為空閒。
     * 所有使用連接池獲得的資料庫連接均應在不使用此連接時返回它。
     *
     * @param conn
     * @return boolean
     */
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

    /**
     * 刷新連接池中所有的連線物件
     */
    public synchronized void refreshConnections() throws SQLException {
        if (connections == null) {
            //log.error("連接池不存在,無法刷新!");
            return;
        }
        PooledConnection pConn = null;
        Enumeration em = connections.elements();
        while (em.hasMoreElements()) {
            pConn = (PooledConnection) em.nextElement();
            // 如果物件忙則等 5 秒 ,5 秒後直接刷新
            if (pConn.isBusy()) {
                wait(5000);
            }
            // 關閉此連接，用一個新的連接代替它。
            closeConnection(pConn.getConnection());
            pConn.setConnection(newConnection());
            pConn.setBusy(false);
        }
    }

    //===============================

    /**
     * 返回連接池的初始大小
     *
     * @return 初始連接池中可獲得的連接數量
     */
    public int getInitialConnections() {
        return this.initialConnections;
    }


    /**
     * 置連接池的初始大小
     *
     * @param initialConnections
     */
    public void setInitialConnections(int initialConnections) {
        this.initialConnections = initialConnections;
    }

    /**
     * 返回連接池自動增加的大小
     *
     * @return 連接池自動增加的大小
     */
    public int getIncrementalConnections() {
        return this.incrementalConnections;
    }


    /**
     * 設置連接池自動增加的大小
     *
     * @param incrementalConnections
     */
    public void setIncrementalConnections(int incrementalConnections) {
        this.incrementalConnections = incrementalConnections;
    }

    /**
     * 返回連接池中最大的可用連接數量
     *
     * @return 連接池中最大的可用連接數量
     */
    public int getMaxConnections() {
        return this.maxConnections;
    }


    /**
     * 設置連接池中最大可用的連接數量
     *
     * @param maxConnections
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * 獲取測試資料庫表的名字
     *
     * @return 測試資料庫表的名字
     */
    public String getTestTable() {
        return this.testTable;
    }

    /**
     * 設置測試表的名字
     *
     * @param testTable String 測試表的名字
     */
    public void setTestTable(String testTable) {
        this.testTable = testTable;
    }

    //===================================

    /**
     * 創建由 numConnections 指定數目的資料庫連接 , 並把這些連接放入 connections 向量中
     *
     * @param numConnections 要創建的資料庫連接的數目
     */
    private void createConnections(int numConnections) throws SQLException {
        for (int x = 0; x < numConnections; x++) {
            // 如果 maxConnections 為 0 或負數，表示連接數量沒有限制,如果連接數己經達到最大，即退出。
            if (this.maxConnections > 0 && this.connections.size() >= this.maxConnections) {
                break;
            }

            // 增加一個連接到連接池中（向量 connections 中）
            try {
                connections.addElement(new PooledConnection(newConnection()));
            } catch (SQLException e) {
                //log.error("創建資料庫連接失敗! ", e);
                System.out.print(e.getMessage());
                // throw new SQLException();

            }
           // log.info("資料庫連接己創建...");
        }
    }

    /**
     * 創建一個新的資料庫連接並返回它
     *
     * @return 返回一個新創建的資料庫連接
     */
    private Connection newConnection() throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
        Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        conn.setAutoCommit(true);

        // 目前沒有連接己被創建
        if (connections.size() == 0) {
            DatabaseMetaData metaData = conn.getMetaData();
            int driverMaxConnections = metaData.getMaxConnections();

            // 資料庫返回的 driverMaxConnections 若為 0，表示此資料庫沒有最大連接限制，
            // 或資料庫的最大連接限制不知道
            // driverMaxConnections 為返回的一個整數，表示此資料庫允許客戶連接的數目
            // 如果連接池中設置的最大連接數量大於資料庫允許的連接數目 , 則置連接池的最大
            // 連接數目為資料庫允許的最大數目
            if (driverMaxConnections > 0 && this.maxConnections > driverMaxConnections) {
                this.maxConnections = driverMaxConnections;
            }
        }
        return conn;
    }

    /**
     * 本函數從連接池向量 connections 中返回一個可用的的資料庫連接，如果
     * 當前沒有可用的資料庫連接，本函數則根據 incrementalConnections 設置
     * 的值創建幾個資料庫連接，並放入連接池中。
     * 如果創建後，所有的連接仍都在使用中，則返回 null
     *
     * @return 返回一個可用的資料庫連接
     */
    private Connection getFreeConnection() throws SQLException {
        // 從連接池中獲得一個可用的資料庫連接
        Connection conn = findFreeConnection();
        if (conn == null) {
            createConnections(incrementalConnections);
            // 重新從池中查找是否有可用連接
            conn = findFreeConnection();
            if (conn == null) {
                // 如果創建連接後仍獲得不到可用的連接，則返回 null
                return null;
            }
        }
        return conn;
    }

    /**
     * 查找連接池中所有的連接，查找一個可用的資料庫連接，
     * 如果沒有可用的連接，返回 null
     *
     * @return 返回一個可用的資料庫連接
     */
    private Connection findFreeConnection() throws SQLException {
        Connection conn = null;
        PooledConnection pConn = null;
        Enumeration em = connections.elements();

        // 遍歷所有的物件，看是否有可用的連接
        while (em.hasMoreElements()) {
            pConn = (PooledConnection) em.nextElement();
            if (!pConn.isBusy()) {
                conn = pConn.getConnection();
                pConn.setBusy(true);

                // 測試此連接是否可用, 此功能用於恢復網路原因斷開的連接
                if (!testConnection(conn)) {
                    // 如果此連接不可再用了，則創建一個新的連接, 並替換此不可用的連線物件，如果創建失敗，返回 null
                    try {
                        conn = newConnection();
                    } catch (SQLException e) {
                        //log.error("創建資料庫連接失敗!", e);
                        return null;
                    }
                    pConn.setConnection(conn);
                }
                break; // 己經找到一個可用的連接，退出
            }
        }
        return conn; // 返回找到到的可用連接
    }

    /**
     * 測試一個連接是否可用，如果不可用，關掉它並返回 false,否則可用返回 true
     *
     * @param conn 需要測試的資料庫連接
     * @return 返回 true 表示此連接可用， false 表示不可用
     */
    private boolean testConnection(Connection conn) {
        try {
            // 判斷測試表是否存在
            if (testTable.equals("")) {
                // 如果測試表為空，試著使用此連接的 setAutoCommit() 方法
                // 來判斷連接否可用（此方法只在部分資料庫可用，如果不可用 ,
                // 拋出異常）。注意：使用測試表的方法更可靠
                conn.setAutoCommit(true);
            } else { // 有測試表的時候使用測試表測試
                Statement stmt = conn.createStatement();
                stmt.execute("select count(*) from " + testTable);
                stmt.close();
            }
        } catch (SQLException e) {
            // 上面拋出異常，此連接己不可用，關閉它，並返回 false;
            closeConnection(conn);
            return false;
        }
        return true;
    }

    /**
     * 關閉一個資料庫連接
     *
     * @param
     */
    private void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            //log.error("關閉資料庫連接出錯!", e);
        }
    }

    /**
     * 使程式等待給定的毫秒數
     *
     * @param
     */
    private void wait(int mSeconds) {
        try {
            Thread.sleep(mSeconds);
        } catch (InterruptedException e) {
        }
    }

    //////////////////////////////////////////////////////////////////////

    /**
     * 內部使用的用於保存連接池中連線物件的類
     * 此類中有兩個成員，一個是資料庫的連接，另一個是指示此連接是否
     * 正在使用的標誌。
     */
    class PooledConnection {
        // 資料庫連接
        Connection connection = null;
        // 此連接是否正在使用的標誌，預設沒有正在使用
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
