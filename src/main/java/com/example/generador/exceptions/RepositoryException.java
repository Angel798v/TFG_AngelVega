package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class RepositoryException extends Exception{

    private final String message;

    private final String typeError = "Repository";

    public RepositoryException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }

}
