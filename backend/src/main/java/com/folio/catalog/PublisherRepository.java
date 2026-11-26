package com.folio.catalog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface PublisherRepository extends JpaRepository<Publisher, Long> {

    @Query("SELECT p FROM Publisher p WHERE lower(p.name) = lower(:name)")
    Optional<Publisher> findByNameIgnoreCase(String name);
}
