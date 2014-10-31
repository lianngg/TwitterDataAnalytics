package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DBFactory {
    private static ConnectionPool pool;
    private static boolean isStarted = false;

    public DBFactory() {
        if (!isStarted) {
            pool = new ConnectionPool(Property.MySQL_DriverName,
                    Property.MySQL_SourceName,
                    Property.MySQL_Account,
                    Property.MySQL_Password,
                    2000);
            try {
                pool.createPool();

            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public Tweets getTweets(String userid, String tweet_time) {
        Tweets tweets = new Tweets();
        String sql = "SELECT  tweet_id, score, censored_message FROM tweet.tweetmessages  " +
                "where user_id='" + userid + "' and creation_time='" + tweet_time + "';";
        System.out.println(sql);
        try {
            Connection conn = pool.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                tweets.add(new Tweet(rs.getLong(1), rs.getInt(2), rs.getString(3).trim()));
            }
            //    //conn.close();
            //    //stmt.close();
            //    //rs.close();
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return tweets;
    }

    public void initcache(Cache cache, int num) {
        String sql = "SELECT  user_id, creation_time, tweet_id, score, censored_message FROM tweet.tweetmessages  limit " + num + ";";
        try {
            Connection conn = pool.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String key = rs.getString("user_id") + ":" + rs.getString("creation_time");
                if (!cache.getQ2Map().containsKey(key)) {
                    Tweets tweets = new Tweets();
                    tweets.add(new Tweet(rs.getLong(3), rs.getInt(4), rs.getString(5).trim()));
                    cache.getQ2Map().put(key, tweets);
                } else {
                    Tweets tweets = cache.getQ2Map().get(key);
                    tweets.add(new Tweet(rs.getLong(3), rs.getInt(4), rs.getString(5).trim()));
                    cache.getQ2Map().put(key, tweets);
                }

            }

            //conn.close();
            //stmt.close();
            //rs.close();
            return;
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void main(String[] args) {
        //System.out.println(DBFactory.getTweets("106948032", "2014-03-21+15:10:31"));
        Cache cache = new Cache();
        new DBFactory().initcache(cache, 10000000);
        System.out.println(cache.getQ2Map().keySet().size());
    }
}
