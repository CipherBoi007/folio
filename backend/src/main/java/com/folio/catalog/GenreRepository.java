package com.folio.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface GenreRepository extends JpaRepository<Genre, Integer> {

    @Query("SELECT g FROM Genre g WHERE lower(g.name) = lower(:name)")
    Optional<Genre> findByNameIgnoreCase(String name);

    @Query("""
            SELECT new com.folio.catalog.CatalogDtos$GenreCount(g.id, g.name, count(b.id))
              FROM Genre g LEFT JOIN Book b ON b.genre = g AND b.deletedAt IS NULL
             GROUP BY g.id, g.name
             ORDER BY g.name
            """)
    List<CatalogDtos.GenreCount> findAllWithCounts();
}
