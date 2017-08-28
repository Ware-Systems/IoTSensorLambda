package com.deviceiot.lambda.process;

import java.io.*;
import java.util.*;
import com.amazonaws.services.lambda.runtime.*;
import com.deviceiot.lambda.api.*;
import com.deviceiot.lambda.exception.*;
import com.deviceiot.lambda.model.*;

/**
 * Created by admin on 8/22/17.
 */
public class ProcessDataHandler implements Closeable {

    private LambdaLogger logger;

    private RHServiceHelper rhServiceHelper;

    private ProcessDataHandler(String hostPort, String rhUser, String rhPass, String database, String collection, LambdaLogger logger) {
        init(hostPort, rhUser, rhPass, database, collection, logger);
    }

    private void init(String hostPort, String rhUser, String rhPass, String database, String collection, LambdaLogger logger) {
        this.logger = logger;
        rhServiceHelper = new RHServiceHelper(hostPort, rhUser, rhPass, database, collection, logger);
    }

    public static ProcessDataHandler getProcessDataHandler(String hostPort, String rhUser, String rhPass, String database, String collection, LambdaLogger logger) {
        return new ProcessDataHandler(hostPort, rhUser, rhPass, database, collection,logger);
    }

    public void processInput(SensorShadow input) throws DeviceIoTLambdaException {
        List<com.deviceiot.lambda.model.Sensor> sensors = input.getState().getReported().getSensors();
        sensors.stream().forEach(sensor -> {
            SensorDate sensorCurrDate = new SensorDate(true);
           sensor.setCurrentDate(sensorCurrDate);
        });
        storeSensorData(sensors);
    }

    private void storeSensorData(List<Sensor> inputSensors) throws DeviceIoTLambdaException {
        rhServiceHelper.sendRequest(inputSensors);
    }

    @Override
    public void close() {
        rhServiceHelper.close();
    }
}
