package com.folio.catalog;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

/** Book metadata by ISBN from the Open Library Books API. Failures degrade to "not found" rather than erroring. */
@Component
class OpenLibraryClient {

    private static final Logger log = LoggerFactory.getLogger(OpenLibraryClient.class);
    private static final Pattern YEAR = Pattern.compile("(1[0-9]{3}|20[0-9]{2})");

    private final RestClient client;

    OpenLibraryClient() {
        HttpClient http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(6));
        this.client = RestClient.builder().baseUrl("https://openlibrary.org").requestFactory(factory)
                .defaultHeader("User-Agent", "Folio/1.0 (library console)")
                .build();
    }

    Optional<Metadata> lookup(String isbn13) {
        JsonNode root;
        try {
            root = client.get()
                    .uri("/api/books?bibkeys=ISBN:{isbn}&format=json&jscmd=data", isbn13)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            log.warn("Open Library lookup for {} failed: {}", isbn13, e.getMessage());
            return Optional.empty();
        }
        if (root == null) {
            return Optional.empty();
        }
        JsonNode book = root.path("ISBN:" + isbn13);
        if (book.isMissingNode() || book.isNull()) {
            return Optional.empty();
        }
        return Optional.of(new Metadata(
                text(book, "title"),
                text(book, "subtitle"),
                names(book.path("authors"), 10),
                names(book.path("publishers"), 1).stream().findFirst().orElse(null),
                year(text(book, "publish_date")),
                book.path("number_of_pages").isInt() ? book.path("number_of_pages").asInt() : null,
                coverUrl(book.path("cover")),
                names(book.path("subjects"), 6)));
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isString() && !value.asString().isBlank() ? value.asString().trim() : null;
    }

    private static List<String> names(JsonNode array, int limit) {
        List<String> names = new ArrayList<>();
        if (array.isArray()) {
            for (JsonNode item : array) {
                String name = text(item, "name");
                if (name != null && names.size() < limit) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    private static Integer year(String publishDate) {
        if (publishDate == null) {
            return null;
        }
        Matcher matcher = YEAR.matcher(publishDate);
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }

    private static String coverUrl(JsonNode cover) {
        String url = text(cover, "large");
        if (url == null) {
            url = text(cover, "medium");
        }
        return url == null ? null : url.replaceFirst("^http://", "https://");
    }

    record Metadata(String title, String subtitle, List<String> authors, String publisher, Integer publishedYear,
                    Integer pages, String coverUrl, List<String> subjects) {
    }
}
