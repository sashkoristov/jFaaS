package jFaaS.invokers;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.auth.oauth2.*;
import com.google.gson.JsonParser;

public class AsyncGoogleFunctionInvoker extends GoogleFunctionInvoker {
    private String googleToken;
    private String googleServiceAccountKey;

    //Todo change to async invocation
    /**
     * Invokes the Google function.
     *
     * @param function       HttpTrigger of function for HTTPPost request
     * @param functionInputs inputs of the function to invoke
     * @return json result
     */
    public JsonObject invokeFunction(String function, Map<String, Object> functionInputs) throws IOException {
        GenericUrl genericUrl = new GenericUrl(function);
        HttpTransport transport = new NetHttpTransport();
        String jsoninput = new Gson().toJson(functionInputs);
        HttpRequestFactory factory = transport.createRequestFactory();
        HttpContent content = new ByteArrayContent("application/json", jsoninput.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = null;
        HttpResponse response = null;
        StringBuilder responseBuilder = null;

        if (googleServiceAccountKey != null) {
            InputStream serviceAccountStream = new ByteArrayInputStream(googleServiceAccountKey.getBytes(StandardCharsets.UTF_8));

            ServiceAccountCredentials saCreds = ServiceAccountCredentials.fromStream(serviceAccountStream);
            saCreds = (ServiceAccountCredentials) saCreds.createScoped(Arrays.asList("https://www.googleapis.com/auth/iam"));

            IdTokenCredentials tokenCredential = IdTokenCredentials.newBuilder()
                    .setIdTokenProvider(saCreds)
                    .setTargetAudience(function).build();


            HttpCredentialsAdapter adapter = new HttpCredentialsAdapter(tokenCredential);
            factory = transport.createRequestFactory(adapter);
            request = factory.buildPostRequest(genericUrl,content);

        }

        else if(googleToken != null) {
            request = factory.buildPostRequest(genericUrl, content);
            String tokenValue = googleToken.substring(googleToken.indexOf('=')+1, googleToken.indexOf(','));
            request.getHeaders().setAuthorization("Bearer " + tokenValue);

        }
        else {
        request = factory.buildPostRequest(genericUrl, content);

        }

        assert request != null;
        //Setting HTTP request Timeout to 60 Minutes for Cloud Functions that take more time
        request.setReadTimeout(3600000);
        response = request.execute();
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
        return new Gson().fromJson(responseBuilder.toString(), JsonObject.class).getAsJsonObject();
    }
}
