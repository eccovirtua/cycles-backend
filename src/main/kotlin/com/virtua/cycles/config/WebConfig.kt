package com.virtua.cycles.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Mapea la ruta URL '/public/images/**' a la carpeta local 'file:./data/profile_photos/'
        // Esta es la carpeta donde guardaremos las imágenes.
        registry.addResourceHandler("/public/images/**")
            .addResourceLocations("file:./data/profile_photos/")
    }
}