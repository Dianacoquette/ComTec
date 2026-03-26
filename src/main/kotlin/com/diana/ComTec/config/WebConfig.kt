package com.diana.ComTec.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/static/css/")
        registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/")
        registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/static/images/")

//        registry.addResourceHandler("/uploads/**")
//            .addResourceLocations("file:uploads/")
    }
}