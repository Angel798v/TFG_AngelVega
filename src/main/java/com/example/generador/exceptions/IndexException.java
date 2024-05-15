package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class IndexException extends Exception{

    private final String message;

    private final String typeError = "Index Docs";

    public IndexException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }
}
