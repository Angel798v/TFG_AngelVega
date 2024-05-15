package com.example.generador.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import java.util.Locale;

@Configuration
public class LocaleConfig implements WebMvcConfigurer {


    /**
     * Determina el idioma que se está usando en ese momento
     * @return Object
     */
    @Bean
    public LocaleContextResolver localeResolver(){

        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("es"));

        return resolver;
    }


    /**
     * Permite cambiar el idioma que se está usando mediante el parámetro "lang"
     * @return Object
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor(){

        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");

        return interceptor;
    }


    /**
     * Añade un interceptor
     * @param registry Object
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(localeChangeInterceptor());
    }

}
