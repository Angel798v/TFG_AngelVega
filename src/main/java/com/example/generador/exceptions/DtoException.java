package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class DtoException extends Exception{

    private final String message;

    private final String typeError = "Dto";

    public DtoException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }

}
