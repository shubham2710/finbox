package org.example.exception;

public class CyclicDependencyException extends RuntimeException {
    public CyclicDependencyException(String msg) { super(msg); }
}
