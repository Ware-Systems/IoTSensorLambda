package com.deviceiot.lambda;

import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import com.amazonaws.services.kms.*;
import com.amazonaws.services.kms.model.*;
import com.amazonaws.services.lambda.runtime.*;
import com.deviceiot.lambda.exception.*;
import com.deviceiot.lambda.model.*;
import com.deviceiot.lambda.process.*;

/**
 * Created by admin on 8/21/17.
 */
public class DeviceIoTLambda implements RequestHandler<SensorShadow, Object> {

    public String handleRequest(SensorShadow sensorShadow, Context context) {
        ProcessDataHandler pdh = null;
        LambdaLogger logger = context.getLogger();
        logger.log("DeviceIoTLambda : Input Message - " + sensorShadow);
        try {
            String rh_host_port = decryptKey(System.getenv("RH_HOST_PORT"));
            String rh_user = decryptKey(System.getenv("RH_USER"));
            String rh_pass = decryptKey(System.getenv("RH_PASS"));
            String sensor_db = System.getenv("SENSOR_DB");
            String sensor_coll = System.getenv("SENSOR_COLLECTION");
            pdh = ProcessDataHandler.getProcessDataHandler(rh_host_port, rh_user, rh_pass, sensor_db, sensor_coll, logger);
            pdh.processInput(sensorShadow);
        } catch (DeviceIoTLambdaException ex) {
            logger.log("DeviceIoTLambda Error Occurred - Error Msg : " + ex.getErrorMessage() + "Exception : " + ex.getMessage());
        } catch (Exception ex) {
            logger.log("DeviceIoTLambda Error Occurred : " + ex.getMessage());
        } finally {
            if (pdh != null) {
                pdh.close();
            }
        }
        return null;
    }


    private static String decryptKey(String key) {
        byte[] encryptedKey = Base64.getDecoder().decode(key);
        AWSKMS client = AWSKMSClientBuilder.defaultClient();
        DecryptRequest request = new DecryptRequest()
                .withCiphertextBlob(ByteBuffer.wrap(encryptedKey));
        ByteBuffer plainTextKey = client.decrypt(request).getPlaintext();
        return new String(plainTextKey.array(), Charset.forName("UTF-8"));
    }

}
