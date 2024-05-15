package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class ControllerException extends Exception{

    private final String message;

    private final String typeError = "Controller";

    public ControllerException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }

}
