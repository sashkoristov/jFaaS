# jFaaS
### A multi-FaaS toolkit to facilitate development of portable applications

## Example
````
// Create instance of HTTPGETInvoker
FaaSInvoker faaSInvoker = new HTTPGETInvoker();

// Set input parameters for cloud function
Map<String, Object> input = new HashMap<>();
input.put("input", "value");

// Invoke function
JsonObject result;
try {
    result = faaSInvoker.invokeFunction("https://jp-tok.functions.cloud.ibm.com/api/v1/web/xxxxxxxxxxx/default/functionName.json", input);
} catch (IOException e) {
    e.printStackTrace();
}

// Continue with result variable
... 
````
