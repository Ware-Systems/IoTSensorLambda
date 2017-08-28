package com.deviceiot.lambda.exception;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceIoTLambdaException extends Exception {

    private static final long serialVersionUID = 5073654202029216137L;

    private String errorMessage;

    public DeviceIoTLambdaException(Throwable throwable) {
        super(throwable);
    }

    public DeviceIoTLambdaException(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
    }

    @Override
    public String getMessage() {
        if (errorMessage != null) {
            return errorMessage;
        } else {
            return super.getMessage();
        }
    }

}
