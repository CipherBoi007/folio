package com.folio.search;

import com.folio.catalog.BookService;
import com.folio.catalog.CatalogDtos.BookSummary;
import com.folio.member.MemberDtos.MemberSummary;
import com.folio.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search")
public class SearchController {

    private static final int LIMIT = 5;

    private final BookService books;
    private final MemberService members;

    public SearchController(BookService books, MemberService members) {
        this.books = books;
        this.members = members;
    }

    @GetMapping
    @Operation(summary = "Top matches across books and members, for the command palette")
    public Results search(@RequestParam @Size(min = 1, max = 100) String q) {
        if (q.isBlank()) {
            return new Results(List.of(), List.of());
        }
        return new Results(books.quickSearch(q, LIMIT),
                members.search(q, null, PageRequest.of(0, LIMIT, Sort.by("fullName"))).content());
    }

    public record Results(List<BookSummary> books, List<MemberSummary> members) {
    }
}
