
import VMInvokerResources.FunctionInput;
import com.amazonaws.regions.Regions;
import com.google.gson.JsonObject;
import com.sun.media.jfxmedia.logging.Logger;
import dps.invoker.FaaSInvoker;
import dps.invoker.LambdaInvoker;
import dps.invoker.OpenWhiskInvoker;
import dps.invoker.VMInvoker;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class Gateway implements FaaSInvoker {

    private FaaSInvoker lambdaInvoker;
    private String awsAccessKey;
    private String awsSecretKey;
    private Regions currentRegion;

    private FaaSInvoker openWhiskInvoker;
    private String openWhiskKey;

    private VMInvoker vmInvoker;

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
            Logger.logMsg(Logger.ERROR, "Cloud not load credentials file.");
            e.printStackTrace();
        }
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

        } else if (function.contains("functions.cloud.ibm") && openWhiskKey != null){
            if(openWhiskInvoker == null){
                openWhiskInvoker = new OpenWhiskInvoker(openWhiskKey);
            }
            return openWhiskInvoker.invokeFunction(function, functionInputs);
        } else if (function.contains(":VM:") && functionInputs.containsKey(FunctionInput.TASK_DESCRIPTION_PATH) && functionInputs.containsKey(FunctionInput.PRIVATE_KEY_PATH)) {
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