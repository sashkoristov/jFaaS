package jFaaS;

import com.amazonaws.regions.Regions;
import com.google.gson.JsonObject;
import jFaaS.invokers.FaaSInvoker;
import jFaaS.invokers.HTTPGETInvoker;
import jFaaS.invokers.LambdaInvoker;
import jFaaS.invokers.OpenWhiskInvoker;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Gateway implements FaaSInvoker {

    private FaaSInvoker lambdaInvoker;
    private String awsAccessKey;
    private String awsSecretKey;
    private Regions currentRegion;

    private FaaSInvoker openWhiskInvoker;
    private String openWhiskKey;

    private FaaSInvoker httpGETInvoker;

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
            }
            if (properties.containsKey("ibm_api_key")){
                openWhiskKey = properties.getProperty("ibm_api_key");
            }

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Cloud not load credentials file.");
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
    @Override
    public JsonObject invokeFunction(String function, Map<String, Object> functionInputs) throws IOException {
        if (function.contains("arn:") && awsSecretKey != null && awsAccessKey != null) {
            Regions tmpRegion = detectRegion(function);
            if(lambdaInvoker == null || tmpRegion != currentRegion){
                currentRegion = tmpRegion;
                lambdaInvoker = new LambdaInvoker(awsAccessKey, awsSecretKey, currentRegion);
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
            // TODO check for google authentication. Currently no authentication is assumed
            return httpGETInvoker.invokeFunction(function, functionInputs);
        } else if(function.contains("azurewebsites.net")) {
            // TODO check for azure authentication. Currently no authentication is assumed
            return httpGETInvoker.invokeFunction(function, functionInputs);
        } else if(function.contains("fc.aliyuncs.com")) {
            // TODO check for alibaba authentication. Currently no authentication is assumed
            return httpGETInvoker.invokeFunction(function, functionInputs);
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
