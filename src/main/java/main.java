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
            long s = System.currentTimeMillis();
            JsonObject result = gateway.invokeFunction("arn:aws:lambda:us-east-2:717556240325:function:asdfasdf",input);
            long e = System.currentTimeMillis();
            System.out.println("time: "+(e-s));
            System.out.println(result);

            System.out.println("Invoke async...");
            s = System.currentTimeMillis();
            result = gateway.invokeAsyncFunciton("arn:aws:lambda:us-east-2:717556240325:function:asdfasdf",input);
            e = System.currentTimeMillis();
            System.out.println("time: "+(e-s));
            System.out.println(result);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
