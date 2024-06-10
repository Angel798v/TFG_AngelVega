package com.example.generador.web.controller;

import com.example.generador.service.DesignService;
import com.example.generador.service.UrlService;
import com.example.generador.util.ColorPick;
import com.example.generador.util.PaletteColorPick;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DesignController {

    @Autowired
    private DesignService designService;

    @Autowired
    private UrlService urlService;


    /**
     * Vista con el apartado del diseño de la aplicación a generar
     * @param model Model
     * @return Vista
     */
    @GetMapping("/Design")
    public String vistaDesign(Model model){

        urlService.setUrl("/Design");

        if(designService.getColores() == null){
            designService.setColores(new ColorPick("#8ED8F1","#FFFFFF", "#FEF9C7",true, true));
            model.addAttribute("colores",designService.getColores());
        }else{
            model.addAttribute("colores",designService.getColores());
        }

        model.addAttribute("nav", designService.isNav());

        return "/Generador/Diseño/Design";
    }


    /**
     * Vista que permite seleccionar los colores principales de la aplicación a generar.
     * @param model Model
     * @return Vista
     */
    @GetMapping("/colorPicker")
    public String vistaColorPicker(Model model){

        urlService.setUrl("/colorPicker");

        ColorPick initialColorPick = new ColorPick();
        initialColorPick.setTextDark(false);
        model.addAttribute("colorPicker", initialColorPick);

        model.addAttribute("paletteColorPick", new PaletteColorPick());

        return "Generador/Diseño/ColorPicker";
    }


    /**
     * Método que establece los colores principales de la aplicación a generar.
     * @param colorPick Objeto ColorPick
     * @return Vista design
     */
    @PostMapping("/colorPicker")
    public String colorPicker(ColorPick colorPick){

        designService.setColores(colorPick);

        System.out.println(colorPick.isTextNavDark());

        return "redirect:/Design";
    }

    @GetMapping("/predefinedColorPicker/{eleccion}")
    public String predefinedColorPicker(@PathVariable(name = "eleccion") String paletteColorPick){

        ColorPick colorPick;

        switch (paletteColorPick){
            case "primera":
                colorPick = new ColorPick("#026670","#FEF9C7","#9FEDD7", true, false);
                break;
            case "segunda":
                colorPick = new ColorPick("#1F2833","#C5C6C7","#66FCF1", true, false);
                break;
            case "tercera":
                colorPick = new ColorPick("#E98074","#EAE7DC","#D8C3A5", true, true);
                break;
            case "cuarta":
                colorPick = new ColorPick("#FAED26","#9D8D8F","#5A5560", false, true);
                break;
            default:
                colorPick = designService.getColores();
        }

        designService.setColores(colorPick);

        return "redirect:/Design";
    }

    /**
     * Método que modifica el valor del atributo nav.
     * @return Vista design
     */
    @PostMapping("/switchNav")
    public String switchNav(){

        designService.switchNav();

        return "redirect:/Design";
    }

}
