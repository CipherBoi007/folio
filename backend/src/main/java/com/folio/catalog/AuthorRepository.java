package com.folio.catalog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface AuthorRepository extends JpaRepository<Author, Long> {

    @Query("SELECT a FROM Author a WHERE lower(a.name) = lower(:name)")
    Optional<Author> findByNameIgnoreCase(String name);
}
