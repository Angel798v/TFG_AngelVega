package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class HtmlException extends Exception{

    private final String message;

    private final String typeError = "HTML";

    public HtmlException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }

}
