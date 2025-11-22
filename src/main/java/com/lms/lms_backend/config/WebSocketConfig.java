// package com.lms.lms_backend.config;

// import org.springframework.context.annotation.Configuration;
// import org.springframework.messaging.simp.config.MessageBrokerRegistry;
// import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
// import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
// import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
// import org.springframework.lang.NonNull; // 1. ADD THIS IMPORT

// @Configuration
// @EnableWebSocketMessageBroker
// public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

//     @Override
//     // 2. ADD @NonNull TO THE PARAMETER
//     public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
//         // "topic" is for broadcasts (everyone)
//         // "queue" is for private messages (one user)
//         config.enableSimpleBroker("/topic", "/queue");
//         config.setApplicationDestinationPrefixes("/app");
//         config.setUserDestinationPrefix("/user"); // This enables user-specific messages
//     }

//     @Override
//     // 3. ADD @NonNull TO THE PARAMETER
//     public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
//         // This is the endpoint the frontend will connect to
//         registry.addEndpoint("/ws")
//                 .setAllowedOrigins("http://localhost:5173") // Allow our React app
//                 .withSockJS();
//     }
// }

//newly added
package com.lms.lms_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        // "topic" is for broadcasts (everyone)
        // "queue" is for private messages (one user)
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user"); 
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // This is the endpoint the frontend will connect to
        registry.addEndpoint("/ws")
                // --- FIX: Add your Vercel URL here ---
                .setAllowedOrigins(
                    "http://localhost:5173", 
                    "http://127.0.0.1:5173",
                    "https://lms-bhadrak.vercel.app" // Your live frontend
                ) 
                .withSockJS();
    }
}