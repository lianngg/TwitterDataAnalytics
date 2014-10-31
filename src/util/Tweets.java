package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Tweets {
    public List<Tweet> tweets = new ArrayList<Tweet>();

    public Tweets() {
    }

    public void append(Tweet tweet) {
        this.tweets.add(tweet);
    }

    public String toString() {
        String r = "";
        Collections.sort(tweets);
        for (Tweet t : tweets)
            r += t;
        return r;
    }

    public void add(Tweet t) {
        tweets.add(t);
    }

    public static void main(String[] args) {
        Tweets t1 = new Tweets();
        t1.add(new Tweet(123, 50, "99"));
        t1.add(new Tweet(456, 50, "99"));
        t1.add(new Tweet(789, 50, "99"));
        Tweets t2 = new Tweets();
        Tweets t3 = new Tweets();
        t3.add(new Tweet(456, 50, "99"));
        t3.add(new Tweet(789, 50, "99"));
        Tweets t4 = new Tweets();
        t4.add(new Tweet(456, 50, "99"));
        System.out.print(t1);
        System.out.print(t2);
        System.out.print(t3);
        System.out.print(t4);
        System.out.print("------------------------");


    }
}
