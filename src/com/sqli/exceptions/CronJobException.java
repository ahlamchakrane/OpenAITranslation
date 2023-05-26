package com.sqli.exceptions;

public class CronJobException extends Exception {
    public CronJobException(String message) {
        super(message);
    }

    public CronJobException(String message, Throwable cause) {
        super(message, cause);
    }
}
