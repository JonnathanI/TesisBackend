package com.duolingo.clone.language_backend.controller

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Esto hace que http://localhost:8081/uploads/ sea accesible
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/")
    }
}