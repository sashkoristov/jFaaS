package jFaaS.invokers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jFaaS.utils.PairResult;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OpenWhiskInvoker implements FaaSInvoker {

    private String key;

    /**
     * Default constructor for openwhisk
     *
     * @param key to authenticate
     */
    public OpenWhiskInvoker(String key) {
        this.key = key;
    }

    /**
     * invoke cloud function
     *
     * @param function       identifier of the function
     * @param functionInputs input parameters
     *
     * @return json result
     *
     * @throws IOException on failure
     */
    @Override
    public PairResult<String, Long> invokeFunction(String function, Map<String, Object> functionInputs) throws IOException {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("Authorization", "Basic " + key);
        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        header.put("Accept-Language", "en-At");
        String functionParameters = "?blocking=true&result=true";

        String body = new Gson().toJson(functionInputs);
        HttpPost post = new HttpPost(function + functionParameters);
        StringEntity entity;
        try {
            entity = new StringEntity(body);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        post.setEntity(entity);
        header.forEach(post::addHeader);

        HttpClient httpClient = getHttpCLientForSSL();
        HttpResponse response;

        long start = System.currentTimeMillis();
        response = httpClient.execute(post);

        try {
            InputStream inputStream = response.getEntity().getContent();
            String stringResponse = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            inputStream.close();
            return new PairResult<>(new Gson().fromJson(stringResponse, JsonObject.class).toString(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private HttpClient getHttpCLientForSSL() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

        }};

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            SSLConnectionSocketFactory sslfactory = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);

            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslfactory)
                    .setConnectionTimeToLive(10, TimeUnit.SECONDS).build();

            return httpClient;

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        return HttpClients.createDefault();
    }
}
