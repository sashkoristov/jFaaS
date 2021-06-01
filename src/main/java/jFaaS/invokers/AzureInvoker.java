package jFaaS.invokers;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jFaaS.utils.PairResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AzureInvoker implements FaaSInvoker {

    String azureKey;


    /**
     * Constructor that creates AzureInvoker object without authentication info
     */
    public AzureInvoker() { azureKey = null; }

    /**
     * Constructor that creates AzureInvoker object with access key
     *
     * @param azureKey A valid host_key that gives access to the azure function app containing the function
     */
    public AzureInvoker(String azureKey) {
        this.azureKey = azureKey;
    }


    /**
     * Invokes the Microsoft Azure function.
     *
     * @param function       HttpTrigger of function for HTTPPost request
     * @param functionInputs inputs of the function to invoke
     *
     * @return json result
     */
    @Override
    public PairResult<String, Long> invokeFunction(String function, Map<String, Object> functionInputs) throws IOException {
        GenericUrl genericUrl = new GenericUrl(function);
        HttpTransport transport = new NetHttpTransport();
        String jsoninput = new Gson().toJson(functionInputs);
        HttpRequestFactory factory = transport.createRequestFactory();
        HttpContent content = new ByteArrayContent("application/json", jsoninput.getBytes(StandardCharsets.UTF_8));
        StringBuilder responseBuilder = null;

        HttpRequest request = factory.buildPostRequest(genericUrl, content);

        //Setting HTTP request Timeout to 60 Minutes for Cloud Functions that take more time
        request.setReadTimeout(3600000);
        HttpHeaders headers = new HttpHeaders();
        if(azureKey != null){
        headers.set("x-functions-key", azureKey);
        }
        request.setHeaders(headers);

        long start = System.currentTimeMillis();
        HttpResponse response = request.execute();
        assert response != null;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getContent()));
            String inputLine;
            responseBuilder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert responseBuilder != null;

        return new PairResult<>(new Gson().fromJson(responseBuilder.toString(), JsonObject.class).getAsJsonObject().toString(), System.currentTimeMillis() - start);
    }
}
