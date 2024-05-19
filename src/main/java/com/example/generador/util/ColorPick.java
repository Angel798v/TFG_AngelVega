package com.example.generador.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ColorPick {

    private String principalCodeColor;

    private String secondaryCodeColor;

    private String tertiaryCodeColor;

    private boolean textDark;
}
