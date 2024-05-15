package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class ConfigException extends Exception{

    private final String message;

    private final String typeError = "Configuration";

    public ConfigException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }
}
