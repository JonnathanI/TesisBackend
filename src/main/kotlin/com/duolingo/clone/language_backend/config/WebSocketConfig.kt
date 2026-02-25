package com.duolingo.clone.language_backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // 📌 prefijos donde el cliente se suscribe (broker simple en memoria)
        registry.enableSimpleBroker("/topic", "/queue")

        // 📌 prefijo para mensajes que el cliente ENVÍA al backend
        registry.setApplicationDestinationPrefixes("/app")

        // 📌 prefijo para destinos por usuario (convertAndSendToUser -> /user/queue/...)
        registry.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // 📌 endpoint WebSocket que usará el frontend
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")  // puedes afinarlo si quieres solo localhost:3000
            .withSockJS()                   // soporte SockJS (fallback)
    }
}