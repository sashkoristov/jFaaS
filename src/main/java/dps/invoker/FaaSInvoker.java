package dps.invoker;

import java.util.Map;

/**
 * FaaS invoker interface
 */
public interface FaaSInvoker {

	String invokeFunction(String function, Map<String, Object> functionInputs) throws Exception;
}
