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

                // ✅ RUTAS DE WEBSOCKET (SockJS usa /ws, /ws/info, /ws/xxxx)
                auth.requestMatchers("/ws/**").permitAll()

                // 1. RUTAS PÚBLICAS
                auth.requestMatchers("/api/auth/**").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/init/**").permitAll()
                auth.requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/debug/fcm/test").permitAll()
                auth.requestMatchers("/api/debug/fcm/**").permitAll()
                // 2. RUTAS PROFESOR/ADMIN
                auth.requestMatchers(HttpMethod.GET, "/api/teacher/classrooms/{id}")
                    .hasAnyAuthority("TEACHER", "ADMIN", "STUDENT")
                auth.requestMatchers("/api/teacher/**").hasAnyAuthority("TEACHER", "ADMIN")
                auth.requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                // 3. RUTAS ESTUDIANTE Y COMUNES
                auth.requestMatchers("/api/student/**").authenticated()
                auth.requestMatchers("/api/progress/**").authenticated()
                auth.requestMatchers("/api/shop/**").authenticated()
                auth.requestMatchers("/api/users/**").authenticated()

                // 4. OTROS
                auth.requestMatchers("/h2-console/**", "/error").permitAll()
                auth.anyRequest().authenticated()
            }

            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOrigins = listOf(
            "http://localhost:3000",
            "https://tesisfront-26h1.onrender.com"
        )

        configuration.allowedMethods = listOf(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE",
            "OPTIONS"
        )

        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}