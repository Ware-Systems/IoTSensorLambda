package com.deviceiot.lambda.api;

import java.io.*;
import java.nio.charset.*;
import java.security.cert.*;
import java.time.*;
import java.util.*;
import javax.net.ssl.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.config.*;
import org.apache.http.config.*;
import org.apache.http.conn.*;
import org.apache.http.conn.socket.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.*;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.retry.backoff.*;
import org.springframework.retry.policy.*;
import org.springframework.retry.support.*;
import org.springframework.web.client.*;
import com.amazonaws.services.lambda.runtime.*;
import com.deviceiot.lambda.exception.*;

public class RHServiceHelper implements Closeable {

    private LambdaLogger logger;

    private String hostPort;

    private String database;

    private String collection;

    private String rhUser;

    private String rhPass;

    HttpClientConnectionManager httpConnectionManager;

    public RHServiceHelper(String hostPort, String rhUser, String rhPass, String database, String collection, LambdaLogger logger) {
        this.hostPort = hostPort;
        this.rhUser = rhUser;
        this.rhPass = rhPass;
        this.database = database;
        this.collection = collection;
        this.logger = logger;
    }

    private CloseableHttpClient createSSLClient() throws DeviceIoTLambdaException {
        CloseableHttpClient httpclient = null;
        try {
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }

            } };

            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE);

            final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslsf)
                    .build();

            httpConnectionManager = new BasicHttpClientConnectionManager(registry);

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(10000)
                    .setSocketTimeout(10000)
                    .build();

            httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .setConnectionManager(httpConnectionManager)
                    .setDefaultRequestConfig(requestConfig)
                    .build();

        } catch (Exception ex) {
            String errorMsg = "Failed to initialise Http SSL Client";
            throw new DeviceIoTLambdaException(errorMsg, ex);
        }
        return httpclient;
    }

    private HttpComponentsClientHttpRequestFactory getHttpComponentsClientHttpRequestFactory() throws DeviceIoTLambdaException {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setHttpClient(createSSLClient());
        return httpRequestFactory;
    }

    private HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }

    private RetryTemplate getRetryTemplate() {
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<>();
        exceptionMap.put(RestClientException.class, true);
        exceptionMap.put(ResourceAccessException.class, true);
        exceptionMap.put(IOException.class, true);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(2, exceptionMap);
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(300);
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

    public String sendRequest(Object requestObj) throws DeviceIoTLambdaException {
        ResponseEntity<String> response = null;
        try {
            Instant before = Instant.now();
            HttpHeaders httpHeaders = createHeaders(rhUser, rhPass);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestObj, httpHeaders);
            RestTemplate restTemplate = new RestTemplate(getHttpComponentsClientHttpRequestFactory());
            String urlToCall = String.format("%s://%s/%s/%s", "https", hostPort, database, collection);
            response = getRetryTemplate().execute(retryContext -> {
                logger.log(" Attempt No --> " + (retryContext.getRetryCount()+1));
                return restTemplate.exchange(urlToCall, HttpMethod.POST, requestEntity, String.class);
            });
            logger.log("RestHeart api call response - " + response);
            Instant after = Instant.now();
            logger.log("RestHeart api response time : " + Duration.between(before, after).toMillis() + " ms");
        } catch (DeviceIoTLambdaException ex) {
            throw ex;
        } catch (Exception ex) {
            String erroMsg = "RestHeart API Call failed";
            throw new DeviceIoTLambdaException(erroMsg, ex);
        }
        return response != null ? response.getBody() : null;
    }

    @Override
    public void close() {
        if(null != httpConnectionManager)
            httpConnectionManager.closeExpiredConnections();
    }
}
