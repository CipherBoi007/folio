package com.folio.catalog;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

final class BookSpecifications {

    private BookSpecifications() {
    }

    static Specification<Book> matching(CatalogDtos.BookFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filter.q() != null && !filter.q().isBlank()) {
                String term = filter.q().trim().toLowerCase(Locale.ROOT);
                String like = "%" + escape(term) + "%";
                String digits = term.replaceAll("[\\s-]", "");

                Subquery<Long> byAuthor = query.subquery(Long.class);
                Root<Book> sub = byAuthor.from(Book.class);
                Join<Book, Author> author = sub.join("authors");
                byAuthor.select(sub.get("id"))
                        .where(cb.equal(sub.get("id"), root.get("id")), cb.like(cb.lower(author.get("name")), like, '\\'));

                List<Predicate> any = new ArrayList<>();
                any.add(cb.like(cb.lower(root.get("title")), like, '\\'));
                any.add(cb.like(cb.lower(root.get("subtitle")), like, '\\'));
                any.add(cb.exists(byAuthor));
                if (digits.matches("\\d{3,13}x?")) {
                    any.add(cb.like(root.get("isbn13"), "%" + digits.replace("x", "") + "%"));
                }
                predicates.add(cb.or(any.toArray(Predicate[]::new)));
            }
            if (filter.genreIds() != null && !filter.genreIds().isEmpty()) {
                predicates.add(root.get("genre").get("id").in(filter.genreIds()));
            }
            if (filter.languageCode() != null && !filter.languageCode().isBlank()) {
                predicates.add(cb.equal(root.get("language").get("code"), filter.languageCode()));
            }
            if (filter.availability() != null) {
                var available = root.<Integer>get("availableQuantity");
                var total = root.<Integer>get("totalQuantity");
                predicates.add(switch (filter.availability()) {
                    case OUT -> cb.equal(available, 0);
                    case IN_STOCK -> cb.greaterThan(available, 0);
                    case LOW -> cb.and(cb.greaterThan(available, 0),
                            cb.or(cb.equal(available, 1), cb.le(cb.prod(available, 5), total)));
                });
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
