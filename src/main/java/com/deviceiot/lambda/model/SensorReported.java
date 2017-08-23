package com.deviceiot.lambda.model;

import java.util.*;

import com.fasterxml.jackson.annotation.*;

import lombok.*;

/**
 * Created by admin on 8/16/17.
 */
@Data
@JsonRootName("reported")
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SensorReported {

    private List<Sensor> sensors;

}
