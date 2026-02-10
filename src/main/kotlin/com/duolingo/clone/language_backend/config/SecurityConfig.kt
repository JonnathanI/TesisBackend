package com.duolingo.clone.language_backend.config

import com.duolingo.clone.language_backend.filter.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val authenticationProvider: AuthenticationProvider // Inyectamos el que creamos en AppConfig
) {

    // --- FILTRO DE SEGURIDAD (RUTAS) ---
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->

                // ✅ PRIMERO: liberar todos los OPTIONS
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 1. RUTAS PÚBLICAS
                auth.requestMatchers("/api/auth/**").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/init/**").permitAll()
                auth.requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()

                // 2. RUTAS PROFESOR/ADMIN
                auth.requestMatchers(HttpMethod.GET, "/api/teacher/classrooms/{id}")
                    .hasAnyAuthority("TEACHER", "ADMIN", "STUDENT")
                auth.requestMatchers("/api/teacher/**").hasAnyAuthority("TEACHER", "ADMIN")
                auth.requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                // 3. RUTAS ESTUDIANTE Y COMUNES
                auth.requestMatchers("/api/student/**").authenticated()
                auth.requestMatchers("/api/progress/**").authenticated()
                auth.requestMatchers("/api/shop/**").authenticated()

                // ❗ ESTA LÍNEA ESTABA BLOQUEANDO EL OPTIONS
                auth.requestMatchers("/api/users/**").authenticated()

                // 4. OTROS
                auth.requestMatchers("/h2-console/**", "/error").permitAll()
                auth.anyRequest().authenticated()
            }

            .authenticationProvider(authenticationProvider) // Usamos el provider de AppConfig
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOrigins = listOf(
            "http://localhost:3000",
            "http://192.168.20.207:3000",
            "https://rex-unantagonised-tommy.ngrok-free.dev"
        )

        // 👇 AQUÍ AGREGAMOS PATCH
        configuration.allowedMethods = listOf(
            "GET",
            "POST",
            "PUT",
            "PATCH",   // 👈 IMPORTANTE
            "DELETE",
            "OPTIONS"
        )

        // Para simplificar, deja que pasen todos los headers del navegador:
        configuration.allowedHeaders = listOf("*")

        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

}