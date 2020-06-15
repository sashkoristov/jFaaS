package jFaaS.invokers;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Map;

/**
 * FaaS invoker interface.
 */
public interface FaaSInvoker {

    /**
     * Invoke a cloud function.
     *
     * @param function       identifier of the function
     * @param functionInputs input parameters
     * @return JsonObject as result
     * @throws IOException on failure
     */
    JsonObject invokeFunction(String function, Map<String, Object> functionInputs) throws IOException;
}
