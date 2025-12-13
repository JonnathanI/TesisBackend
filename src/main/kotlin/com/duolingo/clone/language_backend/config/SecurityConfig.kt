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
                // 1. RUTAS PÚBLICAS
                auth.requestMatchers("/api/auth/**").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/init/**").permitAll() // DataLoader manual si existe
                auth.requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()

                // 2. RUTAS PROFESOR/ADMIN
                auth.requestMatchers("/api/teacher/**").hasAnyAuthority("TEACHER", "ADMIN")
                auth.requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                // 3. RUTAS ESTUDIANTE Y COMUNES
                auth.requestMatchers("/api/student/**").authenticated()
                auth.requestMatchers("/api/progress/**").authenticated()
                auth.requestMatchers("/api/shop/**").authenticated()
                auth.requestMatchers("/api/users/**").authenticated() // Perfil, avatar, etc.

                // 4. OTROS
                auth.requestMatchers("/h2-console/**", "/error").permitAll()
                auth.anyRequest().authenticated()
            }
            .authenticationProvider(authenticationProvider) // Usamos el provider de AppConfig
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    // --- CONFIGURACIÓN CORS (Para que React se conecte) ---
    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration()

        // Ajusta estos puertos a los que use tu React
        configuration.allowedOrigins = listOf("http://localhost:3000", "http://localhost:5173", "http://localhost:5092")

        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}