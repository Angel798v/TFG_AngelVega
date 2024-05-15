package com.example.generador.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
public class Relacion {

    /**
     * Nombre de la primera entidad de la relación
     */
    private EntidadDto A;

    /**
     * Nombre de la segunda entidad de la relación
     */
    private EntidadDto B;


    /**
     * Cardinalidad de la primera entidad de la relación
     */
    private String cardinalityA;

    /**
     * Cardinalidad de la segunda entidad de la relación
     */
    private String cardinalityB;

    /**
     * Indica si la relación es bidireccional, o sino, unidireccional.
     */
    private boolean bidireccional;


    /**
     * Nombre de la entidad A
     */
    @Setter
    private String nameA;

    /**
     * Nombre de la entidad B
     */
    @Setter
    private String nameB;


    /**
     * Constructor de la clase Relación.
     * Crea la relación con la cardinalidad pasada como parámetro si los valores de la cardinalidad son 0..1, N o M.
     * @param A Cardinalidad de la primera entidad de la relación
     * @param B Cardinalidad de la segunda entidad de la relación
     */
    public Relacion(EntidadDto A, EntidadDto B, String cardinalityA, String cardinalityB, boolean bidireccional){

        this.A = A;
        this.B = B;

        if(cardinalityA.equals("0..1") || cardinalityA.equals("N") || cardinalityA.equals("M")){
            this.cardinalityA = cardinalityA;
        }

        if(cardinalityB.equals("0..1") || cardinalityB.equals("N") || cardinalityB.equals("M")){
            this.cardinalityB = cardinalityB;
        }

        this.bidireccional = bidireccional;
    }


    /**
     * Establece la cardinalidad de la primera entidad si es una cardinalidad permitida
     * @param cardinalityA Cardinalidad de la primera entidad de la relación
     */
    public void setCardinalityA(String cardinalityA){
        if(cardinalityA.equals("0..1") || cardinalityA.equals("N") || cardinalityA.equals("M")){
            this.cardinalityA = cardinalityA;
        }
    }

    /**
     * Establece la cardinalidad de la segunda entidad si es una cardinalidad permitida
     * @param cardinalityB Cardinalidad de la segunda entidad de la relación
     */
    public void setCardinalityB(String cardinalityB){
        if(cardinalityB.equals("0..1") || cardinalityB.equals("N") || cardinalityB.equals("M")){
            this.cardinalityB = cardinalityB;
        }
    }

    /**
     * Establece si la relación es unidireccional o bidireccional
     * @param bidireccional true si es bidireccional, false si e unidireccional
     */
    public void setBidireccional(boolean bidireccional){
        this.bidireccional = bidireccional;
    }

    /**
     * Establece el nombre de la primera entidad de la relacion
     * @param A Nombre
     */
    public void setA(EntidadDto A){
        this.A = A;
    }

    /**
     * Establece el nombre de la segunda entidad de la relacion
     * @param B Nombre
     */
    public void setB(EntidadDto B){
        this.B = B;
    }







}
