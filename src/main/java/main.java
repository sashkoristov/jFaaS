import com.google.gson.JsonObject;
import jFaaS.Gateway;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class main {

    public static void main (String[] args){
        Gateway gateway = new Gateway("./credentials.properties");

        Map<String,Object> input = new HashMap<>();
        input.put("input",new Object());

        try {
            System.out.println("Invoke sync...");
            JsonObject result = gateway.invokeFunction("arn:aws:lambda:us-east-2:717556240325:function:asdfasdf",input);
            System.out.println(result);

            System.out.println("Invoke async...");
            result = gateway.invokeAsyncFunciton("arn:aws:lambda:us-east-2:717556240325:function:asdfasdf",input);
            System.out.println(result);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
