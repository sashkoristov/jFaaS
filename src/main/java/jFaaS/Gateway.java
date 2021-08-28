package jFaaS;

import com.amazonaws.regions.Regions;
import com.google.gson.JsonObject;
import jFaaS.invokers.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Gateway implements FaaSInvoker {

    private FaaSInvoker lambdaInvoker;
    private FaaSInvoker asyncLambdaInvoker;
    private String awsAccessKey;
    private String awsSecretKey;
    private String awsSessionToken;
    private Regions currentRegion;

    private FaaSInvoker openWhiskInvoker;
    private String openWhiskKey;

    private FaaSInvoker googleFunctionInvoker;
    private String googleServiceAccountKey;
    private String googleToken;

    private FaaSInvoker azureInvoker;
    private String azureKey;

    private FaaSInvoker httpGETInvoker;
    private VMInvoker vmInvoker;

    private final static Logger LOGGER = Logger.getLogger(Gateway.class.getName());

    /**
     * Gateway.
     *
     * @param credentialsFile contains credentials for FaaS providers
     */
    public Gateway(String credentialsFile){
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(credentialsFile));
            if (properties.containsKey("aws_access_key") && properties.containsKey("aws_secret_key")){
                awsAccessKey = properties.getProperty("aws_access_key");
                awsSecretKey = properties.getProperty("aws_secret_key");
                if(properties.containsKey("aws_session_token")){
                    awsSessionToken = properties.getProperty("aws_session_token");
                }
            }
            if (properties.containsKey("ibm_api_key")){
                openWhiskKey = properties.getProperty("ibm_api_key");
            }

            if(properties.containsKey("google_sa_key")){
                googleServiceAccountKey = properties.getProperty("google_sa_key");
            }
            if(properties.containsKey("google_token")){
                googleServiceAccountKey = properties.getProperty("google_token");

            }

            if(properties.containsKey("azure_key")){
                azureKey = properties.getProperty("azure_key");

            }

            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not load credentials file.");
            }
        httpGETInvoker = new HTTPGETInvoker();

    }


    /**
     * Gateway.
     */
    public Gateway(){
        httpGETInvoker = new HTTPGETInvoker();
    }


    /**
     * Invoke a cloud function.
     *
     * @param function       identifier of the function
     * @param functionInputs input parameters
     * @return               json result
     * @throws IOException   on failure
     */
    public JsonObject invokeAsyncFunction(String function, Map<String, Object> functionInputs) throws IOException{
        // if it is aws we call the async version
        if (function.contains("arn:") && awsSecretKey != null && awsAccessKey != null) {
            Regions tmpRegion = detectRegion(function);
            if(asyncLambdaInvoker == null || tmpRegion != currentRegion){
                currentRegion = tmpRegion;
                asyncLambdaInvoker = new AsyncLambdaInvoker(awsAccessKey, awsSecretKey, awsSessionToken, currentRegion);
            }
            return asyncLambdaInvoker.invokeFunction(function, functionInputs);

        //  if it is openwhisk we call the sync version
        } else if (function.contains("functions.appdomain.cloud") || function.contains("functions.cloud.ibm")) {
            if(openWhiskKey != null) {
                if (openWhiskInvoker == null) {
                    openWhiskInvoker = new OpenWhiskInvoker(openWhiskKey);
                }
            } else {
                if (openWhiskInvoker == null) {
                    openWhiskInvoker = new OpenWhiskInvoker("");
                }
            }
            return openWhiskInvoker.invokeFunction(function.endsWith(".json") ? function : function + ".json", functionInputs);

        // if it is google we call the async version
        } else if(function.contains("cloudfunctions.net")) {
            if(googleServiceAccountKey != null) {
                if (googleFunctionInvoker == null) {
                    googleFunctionInvoker = new GoogleFunctionInvoker(googleServiceAccountKey, "serviceAccount");
                }
            } else if(googleToken != null) {
                if (googleFunctionInvoker == null) {
                    googleFunctionInvoker = new GoogleFunctionInvoker(googleToken, "token");
                }
            } else {
                return httpGETInvoker.invokeFunction(function, functionInputs);
            }
            return googleFunctionInvoker.invokeFunction(function, functionInputs);

        // if it is azure we call the sync version
        } else if(function.contains("azurewebsites.net")) {
            if(azureKey != null){
                if(azureInvoker == null){
                    azureInvoker = new AzureInvoker(azureKey);
                }
                return azureInvoker.invokeFunction(function, functionInputs);
            }
            return httpGETInvoker.invokeFunction(function, functionInputs);

        // if it is alibaba we call the sync version
        } else if(function.contains("fc.aliyuncs.com")) {
            return httpGETInvoker.invokeFunction(function, functionInputs);
        } else if (function.contains(":VM:")) {
            if (vmInvoker == null) {
                vmInvoker = new VMInvoker();
            }
            return vmInvoker.invokeFunction(function, functionInputs);
        }
        return null;
    }

    /**
     * Invoke a cloud function.
     *
     * @param function       identifier of the function
     * @param functionInputs input parameters
     * @return               json result
     * @throws IOException   on failure
     */
    @Override
    public JsonObject invokeFunction(String function, Map<String, Object> functionInputs) throws IOException {
        if (function.contains("arn:") && awsSecretKey != null && awsAccessKey != null) {
            Regions tmpRegion = detectRegion(function);
            if(lambdaInvoker == null || tmpRegion != currentRegion){
                currentRegion = tmpRegion;
                lambdaInvoker = new LambdaInvoker(awsAccessKey, awsSecretKey, awsSessionToken, currentRegion);
            }
            return lambdaInvoker.invokeFunction(function, functionInputs);

        } else if (function.contains("functions.appdomain.cloud") || function.contains("functions.cloud.ibm")) {
            if(openWhiskKey != null) {
                if (openWhiskInvoker == null) {
                    openWhiskInvoker = new OpenWhiskInvoker(openWhiskKey);
                }
            } else {
                if (openWhiskInvoker == null) {
                    openWhiskInvoker = new OpenWhiskInvoker("");
                }
            }
            return openWhiskInvoker.invokeFunction(function.endsWith(".json") ? function : function + ".json", functionInputs);
        } else if(function.contains("cloudfunctions.net")) {
            if(googleServiceAccountKey != null) {
                if (googleFunctionInvoker == null) {
                    googleFunctionInvoker = new GoogleFunctionInvoker(googleServiceAccountKey, "serviceAccount");
                }
            } else if(googleToken != null) {
                if (googleFunctionInvoker == null) {
                    googleFunctionInvoker = new GoogleFunctionInvoker(googleToken, "token");
                }
            } else {
               return httpGETInvoker.invokeFunction(function, functionInputs);
            }
            return googleFunctionInvoker.invokeFunction(function, functionInputs);

        } else if(function.contains("azurewebsites.net")) {
            if(azureKey != null){
                if(azureInvoker == null){
                    azureInvoker = new AzureInvoker(azureKey);
                }
                return azureInvoker.invokeFunction(function, functionInputs);
            }
            return httpGETInvoker.invokeFunction(function, functionInputs);


        } else if(function.contains("fc.aliyuncs.com")) {
            // TODO check for alibaba authentication. Currently no authentication is assumed
            return httpGETInvoker.invokeFunction(function, functionInputs);
        } else if (function.contains(":VM:")) {
            if (vmInvoker == null) {
                vmInvoker = new VMInvoker();
            }
            return vmInvoker.invokeFunction(function, functionInputs);
        }
        return null;
    }

    /**
     * Detect aws lambda region
     *
     * @param function  arn
     * @return          region
     */
    private static Regions detectRegion(String function) {
        String regionName;
        int searchIndex = function.indexOf("lambda:");
        if (searchIndex != -1) {
            regionName = function.substring(searchIndex + "lambda:".length());
            regionName = regionName.split(":")[0];
            try {
                return Regions.fromName(regionName);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
