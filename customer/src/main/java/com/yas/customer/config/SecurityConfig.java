package com.yas.customer.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class SecurityConfig {
    // @Bean
    // public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    //     return http
    //         .authorizeHttpRequests(auth -> auth
    //             .requestMatchers("/actuator/prometheus", "/actuator/health/**",
    //                 "/swagger-ui", "/swagger-ui/**", "/error", "/v3/api-docs/**").permitAll()
    //             .requestMatchers("/storefront/customer/**").hasRole("CUSTOMER")
    //             .requestMatchers("/storefront/**").permitAll()
    //             .requestMatchers("/backoffice/**").hasRole("ADMIN")
    //             .anyRequest().authenticated())
    //         .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
    //         .build();
    // }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll())
            .addFilterBefore(
                fakeUserFilter(),
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
            )
            .build();
    }

    @Bean
    public OncePerRequestFilter fakeUserFilter() {

        return new OncePerRequestFilter() {

            @Override
            protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain
            ) throws ServletException, IOException {

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        "admin@gmail.com",
                        null,
                        List.of(
                            new SimpleGrantedAuthority("ROLE_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_CUSTOMER")
                        )
                    );

                SecurityContextHolder.getContext()
                    .setAuthentication(auth);

                filterChain.doFilter(request, response);
            }
        };
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverterForKeycloak() {
        Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = jwt -> {
            Map<String, Collection<String>> realmAccess = jwt.getClaim("realm_access");
            Collection<String> roles = realmAccess.get("roles");
            return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        };

        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
