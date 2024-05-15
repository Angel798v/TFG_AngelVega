package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class ModelException extends Exception{

    private final String message;

    private final String typeError = "Model";

    public ModelException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }

}
