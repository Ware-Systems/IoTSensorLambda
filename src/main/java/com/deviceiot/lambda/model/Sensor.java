package com.deviceiot.lambda.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

/**
 * Created by admin on 8/16/17.
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Sensor {

    private String sensorID;

    private String sensorName;

    private Integer sensorState;

    private Float tempreature;

    @JsonProperty("$currentDate")
    private SensorDate currentDate;

}
