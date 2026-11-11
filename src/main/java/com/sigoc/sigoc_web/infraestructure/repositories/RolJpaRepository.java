    package com.sigoc.sigoc_web.infraestructure.repositories;

    import com.sigoc.sigoc_web.infraestructure.entities.RolEntity;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    @Repository
    public interface RolJpaRepository extends JpaRepository<RolEntity, Integer> {
    
    }
