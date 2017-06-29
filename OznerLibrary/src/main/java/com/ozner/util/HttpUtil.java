package com.ozner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by zhiyongxu on 15/10/20.
 */
public class HttpUtil {
    static TrustManager[] xtmArray = new MytmArray[]{new MytmArray()};

    public static String get(String url) throws IOException
    {
        URL my_url = new URL(url);
        HttpURLConnection connection = null;

        connection = (HttpURLConnection) my_url.openConnection();
        connection.setReadTimeout(1000 * 60);

        connection.setConnectTimeout(1000 * 60);

        connection.setDoOutput(false);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        try {
            //connection.connect();

            InputStream inputStream = connection.getInputStream();
            try {
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuffer strBuffer = new StringBuffer();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuffer.append(line);
                }
                return strBuffer.toString();
            } finally {
                inputStream.close();
            }

        } finally {
            connection.disconnect();
        }
    }

    public static String postJSON(String url, String json, String charset) throws IOException {
        URL my_url = new URL(url);
        HttpURLConnection connection = null;

        connection = (HttpURLConnection) my_url.openConnection();
        connection.setReadTimeout(1000 * 60);

        connection.setConnectTimeout(1000 * 60);

        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Length", String.valueOf(json.length()));
        //connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json");
        try {
            //connection.connect();
            OutputStream outputStream = connection.getOutputStream();
            try {
                outputStream.write(json.getBytes());
            } finally {
                outputStream.flush();
                //outputStream.close();
            }

            InputStream inputStream = connection.getInputStream();
            try {
                InputStreamReader reader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuffer strBuffer = new StringBuffer();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuffer.append(line);
                }
                return strBuffer.toString();
            } finally {
                inputStream.close();
            }

        } finally {
            connection.disconnect();
        }

    }


    public static String doPost(String httpUrl, String parms) {
        HttpURLConnection http = null;
        String result = null;
        URL url;
        try {
            url = new URL(httpUrl);
            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                http = (HttpURLConnection) url.openConnection();
                ((HttpsURLConnection) http).setHostnameVerifier(DO_NOT_VERIFY);//不进行主机名确认
            } else {
                http = (HttpURLConnection) url.openConnection();
            }
            http.setConnectTimeout(10000);// 设置超时时间
            http.setReadTimeout(50000);
            http.setRequestMethod("POST");// 设置请求类型为
            http.setDoInput(true);
            http.setDoOutput(true);
            http.setRequestProperty("Content-Length", String.valueOf(parms.length()));
            try {
                OutputStream outputStream = http.getOutputStream();
                try {
                    outputStream.write(parms.getBytes());
                } finally {
                    outputStream.flush();
                    outputStream.close();
                }

                InputStream inputStream = http.getInputStream();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;
                    StringBuffer strBuffer = new StringBuffer();
                    while ((line = bufferedReader.readLine()) != null) {
                        strBuffer.append(line);
                    }
                    result = strBuffer.toString();

                }catch (Exception ex){
                    ex.printStackTrace();
//                    Log.e(TAG, "doPost_ex: "+ex.getMessage());
                }finally {
                    inputStream.close();
                }
            } finally {
                http.disconnect();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
//            Log.e(TAG, "doPost_ex: " + ex.getMessage());
        }
        return result;
    }

    /**
     * 信任所有主机-对于任何证书都不做检查
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android 采用X509的证书信息机制
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, xtmArray, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
            // HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);//
            // 不进行主机名确认
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            // TODO Auto-generated method stub
            // System.out.println("Warning: URL Host: " + hostname + " vs. "
            // + session.getPeerHost());
            return true;
        }
    };

    static class MytmArray implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
