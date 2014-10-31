package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Tweet implements Comparable<Tweet>{
    public long tweet_id = 0;
    public int score;
    public String censored_text;

    public Tweet(long tweet_id, int score, String censored_text) {
        this.tweet_id = tweet_id;
        this.score = score;
        this.censored_text = censored_text;
    }


     @Override
    public int compareTo(Tweet tweet) {
        long target_id = ((Tweet) tweet).tweet_id;
        //ascending order
        return (int) (this.tweet_id - target_id);
    }

    @Override
    public String toString() {
        return this.tweet_id + ":" + this.score + ":" + this.censored_text+"\n";
    }

    public static void main(String[] args) {
        Tweet t1 = new Tweet(123, 50, "99");
        Tweet t2 = new Tweet(-50, 50, "99");
        Tweet t3 = new Tweet(999, 50, "99");
        List<Tweet> tweets = new ArrayList<Tweet>();
        tweets.add(t1);
        tweets.add(t2);
        tweets.add(t3);
        Collections.sort(tweets);
        for(Tweet t:tweets)
            System.out.println(t);



    }

}
