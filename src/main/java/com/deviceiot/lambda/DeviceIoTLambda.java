package com.deviceiot.lambda;

import java.util.*;

import com.amazonaws.services.lambda.runtime.*;

/**
 * Created by admin on 8/21/17.
 */
public class DeviceIoTLambda implements RequestHandler<Map<String, Object>, Object> {

    public String handleRequest(Map<String,Object> input, Context context) {

        for(Map.Entry<String, Object> entry : input.entrySet()){
            System.out.println("Item : " + entry.getKey() + " Value : " + entry.getValue());
        }

        String output = "Hello, " + input + "!";
        return output;
    }

}
