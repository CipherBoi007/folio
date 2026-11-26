package com.folio.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "languages")
public class Language {

    @Id
    @Column(length = 8)
    private String code;

    @Column(nullable = false, length = 60)
    private String name;

    protected Language() {
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
