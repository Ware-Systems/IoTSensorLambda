package com.deviceiot.lambda.process;

import java.io.*;
import java.time.*;
import java.util.*;
import org.bson.*;
import com.amazonaws.services.lambda.runtime.*;
import com.deviceiot.lambda.model.*;
import com.mongodb.*;
import com.mongodb.client.*;

/**
 * Created by admin on 8/22/17.
 */
public class ProcessDataHandler implements Closeable {

    private LambdaLogger logger;

    private MongoClient mongoClient;

    private MongoDatabase mongoDatabase;

    private MongoCollection mongoCollection;

    private boolean debug;

    private ProcessDataHandler(String connectionUri, String database, boolean debug, LambdaLogger logger) {
        init(connectionUri, database, debug, logger);
    }

    private void init(String connectionUri, String database, boolean debug, LambdaLogger logger) {
        this.debug = debug;
        this.logger = logger;
        mongoClient = initConnection(connectionUri);
        mongoDatabase = mongoClient.getDatabase(database);
        mongoCollection = mongoDatabase.getCollection("TempreatureSensor");
    }

    private MongoClient initConnection(String connectionUri) {
        MongoClientURI uri = new MongoClientURI(connectionUri);
        mongoClient = new MongoClient(uri);
        return mongoClient;
    }

    public static ProcessDataHandler getProcessDataHandler(String connectionUri, String database, boolean debug, LambdaLogger logger) {
        return new ProcessDataHandler(connectionUri, database, debug, logger);
    }

    public void processInput(SensorShadow input) {

        List<com.deviceiot.lambda.model.Sensor> sensors = input.getState().getReported().getSensors();
        List <Document> sensorList = new ArrayList <>(sensors.size());

        sensors.stream().forEach(sensor -> {
            Document sensorDoc = new Document();
            sensorDoc.put("sensorID", sensor.getSensorID());
            sensorDoc.put("sensorName", sensor.getSensorName());
            sensorDoc.put("sensorState", sensor.getSensorState());
            sensorDoc.put("tempreature", sensor.getTempreature());
            Date timestamp = Date.from(Instant.now());
            sensorDoc.put("lastModifiedDate", timestamp);
            sensorList.add(sensorDoc);
        });
        storeSensorData(sensorList);

        logger.log("processInput: end");
    }

    private void storeSensorData(List<Document> inputSensors) {
        logger.log("storeSensorData: start");
        mongoCollection.insertMany(inputSensors);
        logger.log("storeSensorData: end");
        inputSensors.stream().forEach(output -> {
            logger.log("Saved Sensor: " + output);
        });
    }

        @Override
    public void close() throws IOException {
        mongoClient.close();
    }
}
