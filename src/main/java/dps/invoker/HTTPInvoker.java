package dps.invoker;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import requests.HTTPRequestFactory;
import requests.IResultConverter;
import requests.ResponseConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class HTTPInvoker implements FaaSInvoker {
	private CloseableHttpClient httpClient;

	/**
	 * Invokes the AWS Lambda function or IBM Cloud action over the API Gateway with
	 * a HTTP request and returns the result as string.
	 */
	@Override
	public String invokeFunction(String function, Map<String, Object> parameters) throws Exception {
		Map<String, String> LAMDA_BASIC_HEADER = new HashMap<String, String>();
		// LAMDA_BASIC_HEADER.put("accept", "application/json") ;
		this.httpClient = HttpClients.custom().setMaxConnPerRoute(40).setConnectionTimeToLive(10, TimeUnit.SECONDS)
				.build();
		IResultConverter<?> resultHandler = ResponseConverter.getResponseConverter("String");
		String body = new Gson().toJson(parameters);

		HttpUriRequest request = HTTPRequestFactory.getPostRequest(function, LAMDA_BASIC_HEADER, body);
		HttpResponse response = null;
		String resStr = null;
		try {
			response = httpClient.execute(request);
			resStr = (String) resultHandler.convertResult(response);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resStr;
	}
}
