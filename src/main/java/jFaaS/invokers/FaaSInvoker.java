package jFaaS.invokers;

import jFaaS.utils.PairResult;

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
     *
     * @return PairResult as result with round trip time
     *
     * @throws IOException on failure
     */
    PairResult<String, Long> invokeFunction(String function, Map<String, Object> functionInputs) throws IOException;
}
