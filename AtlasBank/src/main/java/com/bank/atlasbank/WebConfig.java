package com.bank.atlasbank;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirigir la raíz al inicio
        registry.addRedirectViewController("/", "/inicio.html");
        
        // Mapeos limpios para el módulo de autenticación
        registry.addViewController("/login").setViewName("forward:/auth/login.html");
        registry.addViewController("/registro").setViewName("forward:/auth/registro.html");
        
        // Mapeos limpios para el módulo de cliente
        registry.addViewController("/dashboard").setViewName("forward:/customer/dashboard.html");
        registry.addViewController("/cuentas").setViewName("forward:/customer/cuentas.html");
        registry.addViewController("/transferencias").setViewName("forward:/customer/transferencias.html");
        registry.addViewController("/movimientos").setViewName("forward:/customer/movimientos.html");
        registry.addViewController("/ahorro").setViewName("forward:/customer/ahorro_metas.html");
        registry.addViewController("/perfil").setViewName("forward:/customer/perfil.html");
        registry.addViewController("/tarjetas").setViewName("forward:/customer/tarjetas.html");
        
        // Mapeos limpios para administración
        registry.addViewController("/admin").setViewName("forward:/admin/panel_admin.html");
        registry.addViewController("/admin/monitoreo").setViewName("forward:/admin/admin_transacciones.html");
        registry.addViewController("/admin/usuarios").setViewName("forward:/admin/admin_directorio.html");
    }
}