package com.pechen.matchmaker.client;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by pechen on 13.02.2018.
 */
public class ClientSrevice {

    private static String REGISTER_URL = "http://127.0.0.1:8080/server/match/register/";
    public static AtomicLong userId = new AtomicLong(0);

    public static void main(String[] args) {
        ExecutorService service = Executors.newCachedThreadPool();

        for(int i = 0; i < 2; i++) {
            service.submit(new Runnable() {
                public void run() {
                    while (true){
                        long timeout = ThreadLocalRandom.current().nextLong(1, 10);
                        try {
                            TimeUnit.SECONDS.sleep(timeout);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            int randomRank = ThreadLocalRandom.current().nextInt(1, 30);
                            HttpRequestUtil.sendGet(REGISTER_URL + userId.getAndIncrement() + "/" + randomRank);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

    }
}
