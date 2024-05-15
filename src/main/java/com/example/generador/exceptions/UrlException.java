package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class UrlException extends Exception{

    private final String message;

    private final String typeError = "Internationalization";

    public UrlException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }
}
