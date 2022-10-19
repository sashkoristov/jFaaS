package jFaaS.invokers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import jFaaS.utils.PairResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HTTPPOSTInvoker implements FaaSInvoker {
    public HTTPPOSTInvoker() {
        System.setProperty("https.protocols", "TLSv1.2");
    }

    /**
     * Makes a HTTP POST request.
     *
     * @return
     */
    @Override
    public PairResult<String, Long> invokeFunction(String function, Map<String, Object> parameters) throws IOException {
        long start = System.currentTimeMillis();

        URL obj = new URL(function);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        Gson gson = new Gson();
        Type gsonType = new TypeToken<Map<String, Object>>(){}.getType();
        String requestBody = gson.toJson(parameters,gsonType);

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        // why?
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(requestBody.getBytes("utf-8"), 0, requestBody.length());
        os.flush();
        os.close();

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
