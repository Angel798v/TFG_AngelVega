package com.example.generador.config;

import com.example.generador.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioService usuarioService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http.authorizeHttpRequests()
                .requestMatchers("/css/**").permitAll()
                .requestMatchers("/fonts/**").permitAll()
                .requestMatchers("/images/**").permitAll()
                .requestMatchers("/js/**").permitAll()
                .requestMatchers("/sass/**").permitAll()
                .requestMatchers("/").permitAll()
                .requestMatchers("/index").permitAll()
                .requestMatchers("/layout").hasRole("ADMIN")
                .requestMatchers("/registro").permitAll()
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/Notas").hasAnyRole("TEACHER", "STUDENT", "ADMIN")
                .requestMatchers("/Notas/Get").hasAnyRole("TEACHER","STUDENT", "ADMIN")
                .requestMatchers("/Notas/Post").hasAnyRole("TEACHER", "ADMIN")
                .requestMatchers("/Notas/Put/**").hasAnyRole("TEACHER", "ADMIN")
                .requestMatchers("/Notas/Delete/**").hasAnyRole("TEACHER", "ADMIN")
                .requestMatchers("/Notas/viewNotas").hasAnyRole("TEACHER","STUDENT","ADMIN")
                .anyRequest().authenticated();

        http.formLogin()
                .loginPage("/login")
                .successHandler(successHandler())
                .permitAll()
                .and()
                .logout().invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll();

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(usuarioService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }

    /**
     * Handler para la redirección al hacer login
     * @return Vista
     */
    public AuthenticationSuccessHandler successHandler(){

        return (request, response, authentication) -> response.sendRedirect("index");
    }


}