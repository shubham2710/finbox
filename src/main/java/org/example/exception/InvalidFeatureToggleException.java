package org.example.exception;

public class InvalidFeatureToggleException extends RuntimeException {
    public InvalidFeatureToggleException(String msg) { super(msg); }
}
