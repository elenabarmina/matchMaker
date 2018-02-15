package com.pechen.matchmaker.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * Created by pechen on 15.02.2018.
 */
public class HttpRequestUtil {

    static String sendGet(String targetURL) throws IOException {
        return executeRequest(targetURL, null, "GET");
    }

    private static String executeRequest(String targetURL, String urlParameters, String methodName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        long start = System.currentTimeMillis();

        URL url = null;
        URLConnection connection = null;
        InputStream is = null;
        HttpURLConnection http = null;

        try {
            url = new URL(targetURL);

            connection = url.openConnection();

            http = (HttpURLConnection) connection;
            http.setConnectTimeout(6 * 1000);
            http.setRequestMethod(methodName);
            http.setReadTimeout(10000);
            http.connect();

            is = url.openStream();
            byte[] byteChunk = new byte[4096];
            int n;

            while ((n = is.read(byteChunk)) > 0) {
                baos.write(byteChunk, 0, n);
            }
        }
        catch (ConnectException e) {
            System.out.println("timeout error");
        }
        catch (IOException e) {
            System.out.print("failed while reading bytes");
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConn = (HttpURLConnection) connection;
                try {
                    int statusCode = httpConn.getResponseCode();
                    if (statusCode != 200) {
                        System.out.println("response code error");
                    }
                }catch (SocketTimeoutException e2) {
                    System.out.println("timeout error");
                }
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            http.disconnect();
        }
        return baos.toString();
    }

}
