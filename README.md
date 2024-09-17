# jFaaS - A multi-FaaS toolkit to facilitate development of portable Java applications

## General overview 

jFaaS is a Java toolkit that can be imported in a Java application to allow a portable invocation of serverless functions on all well-known FaaS providers by abstracting all their individual APIs.

jFaaS offers a single interface requires function location (e.g., ARN on AWS) and the function input. For invocation with authentication, jFaaS requires credentials in the form presented bellow.


## Supported portable invocations

Invocations are supported on all well-known FaaS providers with or without authentication:

- With authentication
    - AWS Lambda (LambdaInvoker.java)
    - IBM Cloud Functions (OpenWhiskInvoker.java)
    - Google Cloud Functions (GoogleFunctionInvoker.java)
    - Microsoft Azure Functions (AzureInvoker.java)

- Without authentication (HTTPGETInvoker.java) - all FaaS providers that support function invocations with HTTP GET requests
    - AWS Lambda
    - IBM Cloud Functions
    - Google Cloud Functions
    - Microsoft Azure Functions
    - Alibaba 
    - etc

Test phase - invoking tasks into a VM (VMInvoker.java)
- Amazon EC2
- Azure VMs

## Build
````
gradle shadowJar
````
The generated **jFaaS-all.jar** file can be found in the **build/libs/** folder.


## Example(s)

#### Structure ``credentials.proporties``, which should be placed in the same folder as the **jFaaS-all.jar**:
````
aws_access_key_id=
aws_secret_access_key=
aws_session_token=              // (needed for AWS Educate)
ibm_api_key=
google_sa_key={\
 "type": "",\
 "project_id": "",\
 "private_key_id": "",\
 "private_key": "-----BEGIN PRIVATE KEY-----\\n ... \\n-----END PRIVATE KEY-----\\n",\
 "client_email": "",\
 "client_id": "",\
 "auth_uri": "",\
 "token_uri": "",\
 "auth_provider_x509_cert_url": "",\
 "client_x509_cert_url": ""\
}
azure_key=
````

#### Gateway (preferrable)
````
// Create instance of Gateway
Gateway gateway = new Gateway("path/to/credentials.properties");

// Set input parameters for cloud function
Map<String, Object> input = new HashMap<>();
input.put("input", "value");

// Invoke functions
PairResult<String, Long> resultLambda = null;
PairResult<String, Long> resultOpenWhisk = null;
try {
    resultLambda = gateway.invokeFunction("arn:aws:lambda:eu-central-1:xxxxxxxxxxx:function:functionName", input);
    resultOpenWhisk = gateway.invokeFunction("https://jp-tok.functions.cloud.ibm.com/api/v1/web/xxxxxxxxxxx/default/functionName", input);
} catch (IOException e) {
    e.printStackTrace();
}

// Continue with result variables (output of function and Round Trip Time)
String output = resultLambda.getResult();
Long RTT = resultLambda.getRTT();
...
````


#### HTTPGETInvoker
````
// Create instance of HTTPGETInvoker
FaaSInvoker faaSInvoker = new HTTPGETInvoker();

// Set input parameters for cloud function
Map<String, Object> input = new HashMap<>();
input.put("input", "value");

// Invoke function
PairResult<String, Long> result;
try {
    result = faaSInvoker.invokeFunction("https://jp-tok.functions.cloud.ibm.com/api/v1/web/xxxxxxxxxxx/default/functionName.json", input);
} catch (IOException e) {
    e.printStackTrace();
}

// Continue with result variable (output of function and Round Trip Time)
String output = resultLambda.getResult();
Long RTT = resultLambda.getRTT();
... 
````


# Contributions

This project resulted as a part of several bachelor theses at department of computer science, University of Innsbruck, supervised by Dr. Sashko Ristov (sashko@dps.uibk.ac.at):

- "G2GA: Portable execution of workflows in Google Cloud Functions across multiple FaaS platforms", Anna Kapeller and Felix Petschko, SS2021 (Google and Azure function invokers)
- "Running workflow applications across multiple cloud providers", Marina Aichinger, SS2020 (AWS and Azure VM invoker)
- "Multi-provider enactment engine (EE) for serverless workflow applications", Jakob Nöckl, Markus Moosbrugger, SS2019. `Among top three theses for 2019` at the institute of computer science. (AWS and IBM function invokers)

# Support

If you need any additional information, please do not hesitate to contact sashko@dps.uibk.ac.at.

