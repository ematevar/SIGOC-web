package com.sigoc.sigoc_web.infrastructure.config;

import com.sigoc.sigoc_web.infrastructure.security.AutenticacionInterceptor;
import com.sigoc.sigoc_web.infrastructure.security.UsuarioSesionArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AutenticacionInterceptor autenticacionInterceptor;
    private final UsuarioSesionArgumentResolver usuarioSesionArgumentResolver;

    public WebConfig(AutenticacionInterceptor autenticacionInterceptor,
                     UsuarioSesionArgumentResolver usuarioSesionArgumentResolver) {
        this.autenticacionInterceptor = autenticacionInterceptor;
        this.usuarioSesionArgumentResolver = usuarioSesionArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autenticacionInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(usuarioSesionArgumentResolver);
    }
}
