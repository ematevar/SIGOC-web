package com.sigoc.sigoc_web.infraestructure.config;

import com.sigoc.sigoc_web.aplication.usecases.ListarRoles;
import com.sigoc.sigoc_web.domain.ports.out.RolPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CasosUsoConfig {

    @Bean
    public ListarRoles listarRoles(RolPort port) {
        return new ListarRoles(port);
    }
}
