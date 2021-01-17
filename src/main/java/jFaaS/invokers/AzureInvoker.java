package jFaaS.invokers;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AzureInvoker implements  FaaSInvoker{

    String azureKey;

    /**
     * Constructor that creates AzureInvoker object with access key
     * @param azureKey         A valid host_key that gives access to the azure function app containing the function
     */
    public AzureInvoker(String azureKey){
        this.azureKey = azureKey;
    }


    /**
     * Invokes the Microsoft Azure function.
     *
     * @param function       HttpTrigger of function for HTTPPost request
     * @param functionInputs inputs of the function to invoke
     * @return json result
     */
    @Override
    public JsonObject invokeFunction(String function, Map<String, Object> functionInputs) throws IOException {
        GenericUrl genericUrl = new GenericUrl(function);
        HttpTransport transport = new NetHttpTransport();
        String jsoninput = new Gson().toJson(functionInputs);
        HttpRequestFactory factory = transport.createRequestFactory();
        HttpContent content = new ByteArrayContent("application/json", jsoninput.getBytes(StandardCharsets.UTF_8));
        StringBuilder responseBuilder = null;

        HttpRequest request = factory.buildPostRequest(genericUrl, content);
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-functions-key", azureKey);
        request.setHeaders(headers);

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
        }catch (Exception e){
            e.printStackTrace();
        }

        assert responseBuilder != null;
        System.out.println(responseBuilder.toString());
        return new Gson().fromJson(responseBuilder.toString(), JsonObject.class).getAsJsonObject();
    }
}
