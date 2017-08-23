package com.deviceiot.lambda.model;

import com.fasterxml.jackson.annotation.*;

import lombok.*;

/**
 * Created by admin on 8/16/17.
 */
@Data
@JsonRootName("state")
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SensorState {

    private SensorReported reported;

}
