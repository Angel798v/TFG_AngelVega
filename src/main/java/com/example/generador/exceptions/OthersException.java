package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class OthersException extends Exception{

    private final String message;

    private final String typeError = "Other Docs";

    public OthersException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }

}
