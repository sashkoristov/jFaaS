import jFaaS.Gateway;
import jFaaS.invokers.HTTPGETInvoker;
import jFaaS.invokers.HTTPPOSTInvoker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class GatewayTests {

    @Mock
    public static HTTPPOSTInvoker httpPOSTInvoker;

    @Mock
    public static HTTPGETInvoker httpGETInvoker;

    @InjectMocks
    public static Gateway gateway;


    @Test
    public void testHTTPPOSTInvocation() throws IOException {
        Map<String, Object> params = new HashMap<String, Object>();
        gateway.invokeFunction("HTTP_POST:https://foo.bar/api/v1/items", params);
        Mockito.verify(httpPOSTInvoker).invokeFunction(Mockito.eq("https://foo.bar/api/v1/items"),
                Mockito.anyMap());
    }

    @Test void testHTTPGETInvocation() throws IOException {
        Map<String, Object> params = new HashMap<String, Object>();
        gateway.invokeFunction("HTTP_GET:https://foo.bar/api/v1/items", params);
        Mockito.verify(httpGETInvoker).invokeFunction(Mockito.eq("https://foo.bar/api/v1/items"),
                Mockito.anyMap());
    }

    @Test void testAlibabaInvocation() throws IOException {
        String endpoint = "164901546557.cn-shanghai.fc.aliyuncs.com/2016-08-15/proxy/serviceName/functionName/";
        Map<String, Object> params = new HashMap<String, Object>();
        gateway.invokeFunction(endpoint, params);
        Mockito.verify(httpGETInvoker).invokeFunction(Mockito.eq(endpoint),
                Mockito.anyMap());
    }
}
