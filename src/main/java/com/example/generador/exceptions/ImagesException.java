package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class ImagesException extends Exception{

    private final String message;

    private final String typeError = "Images";

    public ImagesException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }

}
