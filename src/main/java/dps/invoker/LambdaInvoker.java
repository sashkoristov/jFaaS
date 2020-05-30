package dps.invoker;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.Gson;
import dps.invoker.FaaSInvoker;
import java.util.Map;

/**
 * AWS Lambda invoker using AWS SDK
 */
public class LambdaInvoker implements FaaSInvoker {

    private String awsAccessKey;
    private String awsSecretKey;
    private AWSLambda lambda;

    
    /**
     * Basic Constructor that creates an LambdaInvoker for a specific region with standard settings
     */
    public LambdaInvoker(String awsAccessKey, String awsSecretKey, Regions region){
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setMaxErrorRetry(0);
        clientConfiguration.setSocketTimeout(900 * 1000);
        
        AWSLambda awsLambda = AWSLambdaClientBuilder.standard().withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withClientConfiguration(clientConfiguration)
                .build();
        this.lambda = awsLambda;
    }
    
    /**
     * Constructor that creates an LambdaInvoker for a specific region with custom ClientConfiguration
     */
    public LambdaInvoker(String awsAccessKey, String awsSecretKey, Regions region, ClientConfiguration clientConfiguration) {
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        
        AWSLambda awsLambda = AWSLambdaClientBuilder.standard().withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withClientConfiguration(clientConfiguration)
                .build();
        this.lambda = awsLambda;
    }
    
    
    /**
     * Invokes the lambda function
     * @param function       function name or ARN
     * @param functionInputs inputs of the function to invoke
     * @return json result in string format
     */
    public String invokeFunction(String function, Map<String, Object> functionInputs) throws Exception {
        String payload = new Gson().toJson(functionInputs);
        InvokeRequest invokeRequest = new InvokeRequest().withFunctionName(function)
                .withInvocationType(InvocationType.RequestResponse).withPayload(payload);
        
        InvokeResult invokeResult = null;
        invokeResult = this.lambda.invoke(invokeRequest);

        assert invokeResult != null;
        return new String(invokeResult.getPayload().array());
    }
}