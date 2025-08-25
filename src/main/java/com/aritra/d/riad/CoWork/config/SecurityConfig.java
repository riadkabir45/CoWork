package com.aritra.d.riad.CoWork.config;

import com.aritra.d.riad.CoWork.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrfMgr -> {
            csrfMgr.disable();
        });
        
        http.authorizeHttpRequests(authz -> {
            authz.anyRequest().permitAll();
        });
        
        http.addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        
        http.sessionManagement(sessionz -> {
              sessionz.disable();
          });
        
        http.formLogin(formLoginz -> {
              formLoginz.disable();
          });

        http.httpBasic(httpBasicz -> {
              httpBasicz.disable();
          });

        return http.build();
    }
}
