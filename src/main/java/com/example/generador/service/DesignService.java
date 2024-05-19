package com.example.generador.service;

import com.example.generador.util.ColorPick;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
public class DesignService {

    @Getter
    @Setter
    private ColorPick colores;

    /**
     * Booleano que indica true si el diseño va a disponer de nav, false en caso de que disponga de barra lateral.
     */
    @Getter
    private boolean nav = true;

    /**
     * Modifica el valor del booleano nav
     */
    public void switchNav(){
        nav = !nav;
    }




}
