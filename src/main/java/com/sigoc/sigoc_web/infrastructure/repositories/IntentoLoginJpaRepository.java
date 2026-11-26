package com.sigoc.sigoc_web.infrastructure.repositories;

import com.sigoc.sigoc_web.infrastructure.entities.IntentoLoginEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntentoLoginJpaRepository extends JpaRepository<IntentoLoginEntity, String> {
}
