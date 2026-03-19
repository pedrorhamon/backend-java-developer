package com.cmanager.app.application.repository;

import com.cmanager.app.application.domain.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepository extends JpaRepository<Show, String> {

    boolean existsByIdIntegration(Integer idIntegration);

    Page<Show> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
