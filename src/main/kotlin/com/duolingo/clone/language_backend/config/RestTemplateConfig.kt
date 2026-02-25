// com.duolingo.clone.language_backend.config.RestTemplateConfig.kt
package com.duolingo.clone.language_backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()
}