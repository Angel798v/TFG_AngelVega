package com.example.generador.exceptions;

import lombok.Getter;

@Getter
public class RoleException extends Exception{

    private final String message;

    private final String typeError = "Role Docs";

    public RoleException(String message){
        this.message = "Type error: " + typeError + "- Reason: " + message;
    }
}
