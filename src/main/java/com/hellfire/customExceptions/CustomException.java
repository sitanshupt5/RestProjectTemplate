package com.hellfire.customExceptions;

public class CustomException extends RuntimeException{

    public CustomException(Throwable cause) {super(cause);}
    public CustomException(String message, Throwable cause) {super(message, cause);}

    public CustomException(String message) {super(message);}
}
