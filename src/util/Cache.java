package util;

import java.util.HashMap;

public class Cache {
    public  static HashMap<String, String> q1_map;
    public static HashMap<String, Tweets> q2_map;

    public Cache() {
        this.q1_map = new HashMap<String, String>();
        this.q2_map = new HashMap<String, Tweets>();
        init();
    }

    public HashMap<String, String> getQ1Map() {
        return this.q1_map;
    }

    public HashMap<String, Tweets> getQ2Map() {
        return this.q2_map;
    }

    private static void init() {

    }


}
