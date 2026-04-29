package com.jobplatform.controlplane.shared.error;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
