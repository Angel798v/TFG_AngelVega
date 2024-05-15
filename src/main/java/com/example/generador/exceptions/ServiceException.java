package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class ServiceException extends Exception{

    private final String message;

    private final String typeError = "Service";

    public ServiceException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }
}
