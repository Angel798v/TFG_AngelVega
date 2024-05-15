package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class MainClassException extends Exception{

    private final String message;

    private final String typeError = "MainClass";

    public MainClassException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }

}
