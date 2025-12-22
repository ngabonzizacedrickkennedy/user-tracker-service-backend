package com.ngabonzizacedrick.client;

import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;

public class UserTrackerClient {

    private static final String SERVER_URL = "https://localhost:8443/track";
    private static final String CLIENT_KEYSTORE_PATH = "certs/client-keystore.p12";
    private static final String CLIENT_KEYSTORE_PASSWORD = "changeit";
    private static final String TRUSTSTORE_PATH = "certs/truststore.jks";
    private static final String TRUSTSTORE_PASSWORD = "changeit";

    public static void main(String[] args) {
        try {
            SSLContext sslContext = createSSLContext();
            
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
            
            HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
            
            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .build()) {
                
                HttpPatch patch = new HttpPatch(SERVER_URL);
                
                try (CloseableHttpResponse response = httpClient.execute(patch)) {
                    int statusCode = response.getCode();
                    String reasonPhrase = response.getReasonPhrase();
                    
                    System.out.println("Response: " + statusCode + " " + reasonPhrase);
                    
                    if (statusCode == 200) {
                        System.out.println("User activity tracked successfully");
                    } else {
                        System.out.println("Failed to track user activity");
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static SSLContext createSSLContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream keyStoreFile = new FileInputStream(CLIENT_KEYSTORE_PATH)) {
            keyStore.load(keyStoreFile, CLIENT_KEYSTORE_PASSWORD.toCharArray());
        }
        
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (FileInputStream trustStoreFile = new FileInputStream(TRUSTSTORE_PATH)) {
            trustStore.load(trustStoreFile, TRUSTSTORE_PASSWORD.toCharArray());
        }
        
        return SSLContextBuilder.create()
                .loadKeyMaterial(keyStore, CLIENT_KEYSTORE_PASSWORD.toCharArray())
                .loadTrustMaterial(trustStore, null)
                .build();
    }
}