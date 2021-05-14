package jFaaS.invokers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jFaaS.utils.PairResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;


public class HTTPGETInvoker implements FaaSInvoker {

    public HTTPGETInvoker() {
        System.setProperty("https.protocols", "TLSv1.2");
    }

    /**
     * Makes a HTTP GET request.
     *
     * @return
     */
    @Override
    public PairResult<String, Long> invokeFunction(String function, Map<String, Object> parameters) throws IOException {
        String url = function.contains("?") ? function + "&" : function + "?";
        StringBuilder urlBuilder = new StringBuilder(url);
        boolean firstValue = true;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (firstValue) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                firstValue = false;
            } else {
                urlBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        long start = System.currentTimeMillis();
        URL obj = new URL(urlBuilder.toString());
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return new PairResult<>(new Gson().fromJson(response.toString(), JsonObject.class).getAsJsonObject().toString(), System.currentTimeMillis() - start);

    }
}
