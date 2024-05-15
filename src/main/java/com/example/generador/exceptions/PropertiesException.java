package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class PropertiesException extends Exception{

    private final String message;

    private final String typeError = "Properties Docs";

    public PropertiesException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }
}
