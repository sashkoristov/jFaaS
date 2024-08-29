package jFaaS;

import com.fasterxml.jackson.databind.JsonNode;
import jFaaS.invokers.*;
import jFaaS.utils.PairResult;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.regions.Region;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Gateway implements FaaSInvoker {

    private final static Logger LOGGER = Logger.getLogger(Gateway.class.getName());
    private FaaSInvoker lambdaInvoker;
    private String awsAccessKey;
    private String awsSecretKey;
    private String awsSessionToken;
    private Region currentRegion;
    private FaaSInvoker openWhiskInvoker;
    private String openWhiskKey;
    private FaaSInvoker googleFunctionInvoker;
    private String googleServiceAccountKey;
    private String googleToken;
    private FaaSInvoker azureInvoker;
    private String azureKey;
    private FaaSInvoker httpGETInvoker;
    private VMInvoker vmInvoker;

    /**
     * Gateway.
     *
     * @param credentialsFile contains credentials for FaaS providers
     */
    public Gateway(String credentialsFile) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(credentialsFile));
            if (properties.containsKey("aws_access_key") && properties.containsKey("aws_secret_key")) {
                awsAccessKey = properties.getProperty("aws_access_key");
                awsSecretKey = properties.getProperty("aws_secret_key");
                if (properties.containsKey("aws_session_token")) {
                    awsSessionToken = properties.getProperty("aws_session_token");
                }
            }
            if (properties.containsKey("ibm_api_key")) {
                openWhiskKey = properties.getProperty("ibm_api_key");
            }

            if (properties.containsKey("google_sa_key")) {
                googleServiceAccountKey = properties.getProperty("google_sa_key");
            }
            if (properties.containsKey("google_token")) {
                googleServiceAccountKey = properties.getProperty("google_token");

            }

            if (properties.containsKey("azure_key")) {
                azureKey = properties.getProperty("azure_key");

            }

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not load credentials file.");
        }
        httpGETInvoker = new HTTPGETInvoker();

    }

    public Gateway(JsonNode credentials) {
        try {
            if (credentials.has("aws_credentials")) {
                JsonNode awsCredentials = credentials.get("aws_credentials");
                awsAccessKey = awsCredentials.get("access_key").textValue();
                awsSecretKey = awsCredentials.get("secret_key").textValue();
                if (awsCredentials.has("token") && StringUtils.isNotEmpty(awsCredentials.get("token").textValue())) {
                    awsSessionToken = credentials.get("aws_credentials").get("token").textValue();
                }
            }

            if (credentials.has("gcp_credentials")) {
                googleServiceAccountKey = credentials.get("gcp_credentials").toString();
            }

            if (credentials.has("ibm_credentials")) {
                openWhiskKey = credentials.get("ibm_api_key").toString();
            }

            if (credentials.has("azure_credentials")) {
                azureKey = credentials.get("azure_credentials").toString();
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load credentials");
        }

        httpGETInvoker = new HTTPGETInvoker();
    }


    /**
     * Gateway.
     */
    public Gateway() {
        httpGETInvoker = new HTTPGETInvoker();
    }

    /**
     * Detect aws lambda region
     *
     * @param function arn
     *
     * @return region
     */
    private static Region detectRegion(String function) {
        String regionName;
        int searchIndex = function.indexOf("lambda:");
        if (searchIndex != -1) {
            regionName = function.substring(searchIndex + "lambda:".length());
            regionName = regionName.split(":")[0];
            try {
                return Region.of(regionName);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Invoke a cloud function.
     *
     * @param function       identifier of the function
     * @param functionInputs input parameters
     *
     * @return json result
     *
     * @throws IOException on failure
     */
    @Override
    public PairResult invokeFunction(String function, Map<String, Object> functionInputs) throws IOException {
        if (function.contains("arn:") && awsSecretKey != null && awsAccessKey != null) {
            Region tmpRegion = detectRegion(function);
            lambdaInvoker = new LambdaInvoker(awsAccessKey, awsSecretKey, awsSessionToken, tmpRegion);
            return lambdaInvoker.invokeFunction(function, functionInputs);

        } else if (function.contains("functions.appdomain.cloud") || function.contains("functions.cloud.ibm")) {
            if (openWhiskKey != null) {
                if (openWhiskInvoker == null) {
                    openWhiskInvoker = new OpenWhiskInvoker(openWhiskKey);
                }
            } else {
                if (openWhiskInvoker == null) {
                    openWhiskInvoker = new OpenWhiskInvoker("");
                }
            }
            return openWhiskInvoker.invokeFunction(function.endsWith(".json") ? function : function + ".json", functionInputs);
        } else if (function.contains("cloudfunctions.net")) {
            if (googleServiceAccountKey != null) {
                if (googleFunctionInvoker == null) {
                    googleFunctionInvoker = new GoogleFunctionInvoker(googleServiceAccountKey, "serviceAccount");
                }
            } else if (googleToken != null) {
                if (googleFunctionInvoker == null) {
                    googleFunctionInvoker = new GoogleFunctionInvoker(googleToken, "token");
                }
            } else {
                if (googleFunctionInvoker == null) {
                    googleFunctionInvoker = new GoogleFunctionInvoker();
                }
            }
            return googleFunctionInvoker.invokeFunction(function, functionInputs);

        } else if (function.contains("azurewebsites.net")) {
            if (azureKey != null) {
                if (azureInvoker == null) {
                    azureInvoker = new AzureInvoker(azureKey);
                }
                
            } else{
            
                if(azureInvoker == null){

                azureInvoker = new AzureInvoker();
                }
            }
           
            
            return azureInvoker.invokeFunction(function, functionInputs);
            

        } else if (function.contains("fc.aliyuncs.com")) {
            // TODO check for alibaba authentication. Currently no authentication is assumed
            return httpGETInvoker.invokeFunction(function, functionInputs);
        } else if (function.contains(":VM:")) {
            if (vmInvoker == null) {
                vmInvoker = new VMInvoker();
            }
            return vmInvoker.invokeFunction(function, functionInputs);
        } else {
            return httpGETInvoker.invokeFunction(function, functionInputs);
        }
    }

    /**
     * Returns the assigned memory of a function.
     *
     * @param function to return the memory from
     *
     * @return the amount of memory in MB or -1 if the provider is unsupported
     */
    public Integer getAssignedMemory(String function) {
        if (function.contains("arn:") && awsSecretKey != null && awsAccessKey != null) {
            Region tmpRegion = detectRegion(function);
            if (lambdaInvoker == null || tmpRegion != currentRegion) {
                currentRegion = tmpRegion;
                lambdaInvoker = new LambdaInvoker(awsAccessKey, awsSecretKey, awsSessionToken, currentRegion);
            }
            return ((LambdaInvoker) lambdaInvoker).getAssignedMemory(function);
        }
        // TODO implement for different providers
        LOGGER.log(Level.WARNING, "Getting the assigned memory is currently not supported for your provider.");
        return -1;
    }

}
