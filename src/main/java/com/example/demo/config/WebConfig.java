package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Mapeia o link limpo para o ficheiro físico interno usando forward:
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/cadastro").setViewName("forward:/cadastro.html");
        registry.addViewController("/recuperar-senha").setViewName("forward:/recuperar-senha.html");
        registry.addViewController("/redefinir-senha").setViewName("forward:/redefinir-senha.html");
        
        // O painel do admin e a página principal também ganham rotas limpas
        registry.addViewController("/admin-panel").setViewName("forward:/admin.html");
        registry.addViewController("/dashboard").setViewName("forward:/index.html");
    }
}