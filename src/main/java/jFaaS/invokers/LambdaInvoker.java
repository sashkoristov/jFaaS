package jFaaS.invokers;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jFaaS.utils.PairResult;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/**
 * AWS Lambda invoker using AWS SDK.
 */
public class LambdaInvoker implements FaaSInvoker {

    private String awsSessionToken;
    private String awsAccessKey;
    private String awsSecretKey;
    private LambdaClient lambda;

    /**
     * Basic Constructor that creates an LambdaInvoker for a specific region with standard settings.
     *
     * @param awsAccessKey aws access key
     * @param awsSecretKey aws secret key
     * @param region       of the cloud function
     */
    public LambdaInvoker(String awsAccessKey, String awsSecretKey, String awsSessionToken, Region region) {
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        this.awsSessionToken = awsSessionToken;
        ClientOverrideConfiguration clientConfiguration = ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofSeconds(900))
                .retryStrategy(b -> b.maxAttempts(1))
                .build();

        SdkHttpClient httpClient = ApacheHttpClient.builder().maxConnections(10000).build();

        if (awsSessionToken != null) {
            AwsSessionCredentials sessionCredentials = AwsSessionCredentials.create(awsAccessKey, awsSecretKey, awsSessionToken);
            lambda = LambdaClient.builder().region(region)
                    .credentialsProvider(StaticCredentialsProvider.create(sessionCredentials))
                    .httpClient(httpClient)
                    .overrideConfiguration(clientConfiguration)
                    .build();
        } else {
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);
            lambda = LambdaClient.builder().region(region)
                    .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                    .httpClient(httpClient)
                    .overrideConfiguration(clientConfiguration)
                    .build();
        }
    }

    /**
     * Constructor that creates an LambdaInvoker for a specific region with custom ClientConfiguration.
     *
     * @param awsAccessKey        aws access key
     * @param awsSecretKey        aws secret key
     * @param region              of the cloud function
     * @param clientConfiguration custom client configuration
     */
    public LambdaInvoker(String awsAccessKey, String awsSecretKey, Region region, ClientOverrideConfiguration clientConfiguration) {
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(awsAccessKey, awsSecretKey);

        lambda = LambdaClient.builder().region(region)
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .httpClientBuilder(ApacheHttpClient.builder())
                .overrideConfiguration(clientConfiguration)
                .build();
    }

    /**
     * Invokes the lambda function.
     *
     * @param function       function name or ARN
     * @param functionInputs inputs of the function to invoke
     *
     * @return json result
     */
    @Override
    public PairResult invokeFunction(String function, Map<String, Object> functionInputs) throws IOException {
        String payload = new Gson().toJson(functionInputs);
        InvokeRequest invokeRequest = InvokeRequest.builder().functionName(function)
                .invocationType(InvocationType.REQUEST_RESPONSE)
                .payload(SdkBytes.fromString(payload, java.nio.charset.StandardCharsets.UTF_8))
                .build();

        long start = System.currentTimeMillis();
        InvokeResponse invokeResult = lambda.invoke(invokeRequest);

        assert invokeResult != null;
        return new PairResult(new Gson().fromJson(invokeResult.payload().asUtf8String(), JsonObject.class).toString(), System.currentTimeMillis() - start);
    }

    /**
     * Returns the assigned memory of a function.
     *
     * @param function to return the memory from
     *
     * @return the amount of memory in MB
     */
    public Integer getAssignedMemory(String function) {
        return lambda.getFunction(GetFunctionRequest.builder().functionName(function)
                .build()).configuration().memorySize();
    }

}
