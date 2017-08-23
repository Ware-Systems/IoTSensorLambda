package com.deviceiot.lambda;

import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import com.amazonaws.services.kms.*;
import com.amazonaws.services.kms.model.*;
import com.amazonaws.services.lambda.runtime.*;
import com.deviceiot.lambda.model.*;
import com.deviceiot.lambda.process.*;

/**
 * Created by admin on 8/21/17.
 */
public class DeviceIoTLambda implements RequestHandler<SensorShadow, Object> {

    public String handleRequest(SensorShadow sensorShadow, Context context) {
        try {
            LambdaLogger logger = context.getLogger();
            logger.log("DeviceIoTLambda --> execution --> START");
            String mongodb_atlas_cluster_uri = System.getenv("MONGODB_ATLAS_CLUSTER_URI");
            String sensor_db = System.getenv("SENSOR_DB");
            String mongoClusterURIDecrypt = decryptKey(mongodb_atlas_cluster_uri);
            ProcessDataHandler pdh = null;
            try {
                pdh = ProcessDataHandler.getProcessDataHandler(mongoClusterURIDecrypt, sensor_db, true, logger);
                pdh.processInput(sensorShadow);
            } finally {
                if (pdh != null) {
                    pdh.close();
                }
            }
            logger.log("DeviceIoTLambda --> execution --> END");
        } catch (Exception e) {
            context.getLogger().log(e.getMessage());
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

    public static void main(String[] args) {
    }

}
