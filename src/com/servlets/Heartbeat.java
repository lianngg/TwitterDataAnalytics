package com.servlets;

import util.Cache;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Servlet implementation class FirstServlet
 */
@WebServlet(description = "Q1 Servlet", urlPatterns = {"/q1", "/Q1Servlet.do"})
public class Heartbeat extends HttpServlet {
    private static final long serialVersionUID = 1L;
    //Key
    public static BigDecimal prime_x = new BigDecimal("6876766832351765396496377534476050002970857483815262918450355869850085167053394672634315391224052153");
    //Cache for Q1
    public static Cache cache=new Cache();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Heartbeat() throws Exception {
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String prime_str = request.getParameter("key");
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        //If input string is empty or null
        if (prime_str == null || prime_str.length() == 0) {
            PrintWriter Webout = response.getWriter();
            try {
                Webout.println("");
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return;
        }
        //prepare for print out
        PrintWriter Webout = response.getWriter();
        //if there is not hit in the cache
        if (!cache.getQ1Map().containsKey(prime_str)) {
            BigDecimal prime_given = new BigDecimal(prime_str);
            BigDecimal ans = new BigDecimal("0");
            //calculate the value for return
            try {
                ans = prime_given.divide(prime_x);
            } catch (ArithmeticException e) {
                System.out.println("Catch an exception!");
            }
            //put the calculated value in the cache
            cache.getQ1Map().put(prime_str, ans.toString());
            //get the date from system string
            Date date = new Date();
            //format the date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateNowStr = sdf.format(date);
            //print the result
            try {
                Webout.println(ans);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            Webout.println("LLW,2484-1862-6762,3207-8060-5305, 4820-9017-1878");
            Webout.println(dateNowStr);
        } else {// if there is a hit in the cache, return get the value in the cache and return it
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateNowStr = sdf.format(date);
            try {
                Webout.println(Cache.q1_map.get(prime_str));
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            Webout.println("LLW,2484-1862-6762,3207-8060-5305, 4820-9017-1878");
            Webout.println(dateNowStr);
        }
    }



    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
    }

}