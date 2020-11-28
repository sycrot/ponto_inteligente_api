package com.thiagofarias.pontointeligente.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class PontoInteligenteTfApplication {

	public static void main(String[] args) {
		SpringApplication.run(PontoInteligenteTfApplication.class, args);
	}
	
	protected void configure(HttpSecurity http) throws Exception{
        http.csrf().disable()
        .authorizeRequests().antMatchers("/").permitAll();
	}

}
