# jFaaS
### A multi-FaaS toolkit to facilitate development of portable applications

### Currently supports AWS Lambda, IBM Cloud Functions, and all FaaS providers that support function invocations with HTTP GET requests (e.g. AWS, IBM, Google, Microsoft, Alibaba, etc) 

## Build
````
mvn package
````
The generated **.jar** file can be found in the **target/** folder.


## Example(s)

#### Gateway
````
// Create instance of Gateway
Gateway gateway = new Gateway("path/to/credentials.properties");

// Set input parameters for cloud function
Map<String, Object> input = new HashMap<>();
input.put("input", "value");

// Invoke functions
JsonObject resultLambda = null;
JsonObject resultOpenWhisk = null;
try {
    resultLambda = gateway.invokeFunction("arn:aws:lambda:eu-central-1:xxxxxxxxxxx:function:functionName", input);
    resultOpenWhisk = gateway.invokeFunction("https://jp-tok.functions.cloud.ibm.com/api/v1/web/xxxxxxxxxxx/default/functionName", input);
} catch (IOException e) {
    e.printStackTrace();
}

// Continue with result variables
...
````

###### Structure ``credentials.proporties``:
````
aws_access_key=
aws_secret_key=
ibm_api_key=
````

#### HTTPGETInvoker
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
