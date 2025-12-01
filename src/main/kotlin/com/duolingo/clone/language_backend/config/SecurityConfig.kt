package com.duolingo.clone.language_backend.config

import com.duolingo.clone.language_backend.filter.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
// Importaciones necesarias para CORS
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    // --- BEANS ESENCIALES ---

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.getAuthenticationManager()
    }

    // --- CONFIGURACIÓN DE CORS (Cross-Origin Resource Sharing) ---

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOrigins = listOf("http://localhost:5173","http://localhost:3000")

        // 🔥 CORRECCIÓN: Usar .name() o .toString() si Spring no acepta el enum directamente.
        // En Kotlin/Spring, .name o .toString() de un Enum suele funcionar,
        // pero dado que .name está marcado como privado, .toString() es la solución más segura.
        configuration.allowedMethods = listOf(
            HttpMethod.GET.toString(),
            HttpMethod.POST.toString(),
            HttpMethod.PUT.toString(),
            HttpMethod.DELETE.toString(),
            HttpMethod.OPTIONS.toString()
        )

        configuration.allowedHeaders = listOf("Authorization", "Cache-Control", "Content-Type")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    // --- FILTRO DE CADENA DE SEGURIDAD ---

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 🔥 Habilita la configuración de CORS definida arriba
            .cors { it.configurationSource(corsConfigurationSource()) }

            .csrf { it.disable() } // Deshabilita CSRF para APIs REST sin estado
            .sessionManagement {
                // Configura para usar JWT (sin sesiones de servidor)
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                // ... (Tus reglas de autorización se mantienen intactas) ...
                auth
                    // Rutas de Acceso Público (Registro y Login)
                    .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/init/admin").permitAll()
                    .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                    .requestMatchers("/api/teacher/**").hasAnyAuthority("TEACHER", "ADMIN")
                    .requestMatchers("/api/progress/**").authenticated()
                    .requestMatchers("/api/shop/**").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll() // Hacemos GET de cursos público
                    .requestMatchers("/h2-console/**", "/error").permitAll()
                    .anyRequest().authenticated()
            }
            // AÑADE EL FILTRO JWT
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}