package com.example.workshop.config;

import com.example.workshop.service.UserService;
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
	private UserService userService;

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
			.requestMatchers("/registro").permitAll()
			.requestMatchers("/usuarios/**").hasRole("ADMIN")
			.requestMatchers("/Mecanico").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Mecanico/Get").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Mecanico/Post").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Mecanico/Put/**").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Mecanico/Delete/**").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Cliente").hasAnyRole("ADMIN")
			.requestMatchers("/Cliente/Get").hasAnyRole("ADMIN")
			.requestMatchers("/Cliente/Post").hasAnyRole("ADMIN")
			.requestMatchers("/Cliente/Put/**").hasAnyRole("ADMIN")
			.requestMatchers("/Cliente/Delete/**").hasAnyRole("ADMIN")
			.requestMatchers("/Tipo_servicio").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Tipo_servicio/Get").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Tipo_servicio/Post").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Tipo_servicio/Put/**").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Tipo_servicio/Delete/**").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Reparacion").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Reparacion/Get").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Reparacion/Post").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Reparacion/Put/**").hasAnyRole("ADMIN","USER")
			.requestMatchers("/Reparacion/Delete/**").hasAnyRole("ADMIN","USER")
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
		authenticationProvider.setUserDetailsService(userService);
		authenticationProvider.setPasswordEncoder(passwordEncoder());

		return authenticationProvider;
	}

	public AuthenticationSuccessHandler successHandler(){

		return (request, response, authentication) -> response.sendRedirect("index");
	}

}