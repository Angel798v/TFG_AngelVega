package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class DirectoriesException extends Exception{

    private final String message;

    private final String typeError = "Directories";

    public DirectoriesException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }

}
