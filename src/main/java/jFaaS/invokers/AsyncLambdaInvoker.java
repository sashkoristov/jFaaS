package jFaaS.invokers;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Map;

public class AsyncLambdaInvoker extends LambdaInvoker{ 

    private String awsSessionToken;
    private String awsAccessKey;
    private String awsSecretKey;
    private AWSLambda lambda;

    /**
     * Invokes the lambda function.
     *
     * @param function       function name or ARN
     * @param functionInputs inputs of the function to invoke
     * @return json result
     */
    public JsonObject invokeFunction(String function, Map<String, Object> functionInputs) throws IOException {
        String payload = new Gson().toJson(functionInputs);
        InvokeRequest invokeRequest = new InvokeRequest().withFunctionName(function)
                .withInvocationType(InvocationType.RequestResponse).withPayload(payload);

        InvokeResult invokeResult = this.lambda.invoke(invokeRequest);

        assert invokeResult != null;
        return new Gson().fromJson(new String(invokeResult.getPayload().array()), JsonObject.class);
    }
}