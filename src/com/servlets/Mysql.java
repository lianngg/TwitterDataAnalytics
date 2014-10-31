package com.servlets;

import util.Cache;
import util.DBFactory;
import util.Property;
import util.Tweets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 2014/10/22
 * Time: 下午 10:14
 * To change this template use File | Settings | File Templates.
 */
@WebServlet(description = "Mysql", urlPatterns = {"/q2", "/MysqlServlet.do2"})
public class Mysql extends HttpServlet {
    private String mutex = "";
    static DBFactory dbFactory;
    static Cache cache;
    static boolean isEnable = false;

    @Override
    public synchronized void init() throws ServletException {
        //pre-load some data from mysql into cache
        if (!isEnable) {
            cache = new Cache();
            dbFactory = new DBFactory();
            dbFactory.initcache(cache, Property.num);
            isEnable = true;
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //get user id
        String userid = request.getParameter("userid");
        //get tweet time
        String tweet_time = request.getParameter("tweet_time");
        //set data type for return
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (userid == null || userid.length() != 10 || tweet_time == null || tweet_time.length() != 19) {
            try {
                response.getWriter().println("");
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return;
        }
        synchronized (mutex) {
            String[] temp = tweet_time.split(" ");
            String timeStamp = temp[0] + "+" + temp[1];
            String key = userid + ":" + timeStamp;      
            //if there is no hit in the cache get the data from mysql
            if (!cache.getQ2Map().containsKey(key)) {
                Tweets tweets = dbFactory.getTweets(userid, timeStamp);
                response.getWriter().println("LLW,2484-1862-6762,3207-8060-5305, 4820-9017-1878");
                response.getWriter().print(tweets);
                cache.getQ2Map().put(key, tweets);
            } else {//if there is a hit, get the data from cache
                response.getWriter().println("LLW,2484-1862-6762,3207-8060-5305, 4820-9017-1878");
                response.getWriter().print(cache.getQ2Map().get(key));
            }
        }
        
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }


}
