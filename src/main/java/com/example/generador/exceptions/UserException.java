package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class UserException extends Exception{

    private final String message;

    private final String typeError = "User docs";

    public UserException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }
}
