package com.aritra.d.riad.CoWork.config;

import com.aritra.d.riad.CoWork.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrfMgr -> {
            csrfMgr.disable();
        });
        
        http.authorizeHttpRequests(authz -> {
            authz.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/", "/test/**").permitAll()  // <-------------------------------|
                .requestMatchers("/api/auth/**").permitAll()  // <--------------------------------|
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "MODERATOR") // <--|  auth disabled due to fault
                .anyRequest().authenticated();  // <----------------------------------------------------------|
                //.anyRequest().permitAll(); //  <------------------ Allow all requests
        });
        
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // <--  auth disabled due to fault

        http.sessionManagement(session -> {
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
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
