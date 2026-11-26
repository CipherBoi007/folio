package com.folio.common.csv;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

/** RFC 4180 CSV with a UTF-8 BOM so Excel opens Tamil/Hindi text and the ₹ sign correctly. */
public final class CsvWriter {

    private final PrintWriter out;

    private CsvWriter(PrintWriter out) {
        this.out = out;
    }

    public static CsvWriter attachment(HttpServletResponse response, String filename) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        PrintWriter writer = response.getWriter();
        writer.write('﻿');
        return new CsvWriter(writer);
    }

    public CsvWriter row(Object... values) {
        out.write(Stream.of(values).map(CsvWriter::cell).collect(Collectors.joining(",")));
        out.write("\r\n");
        return this;
    }

    public CsvWriter row(List<?> values) {
        return row(values.toArray());
    }

    public void flush() {
        out.flush();
    }

    static String cell(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        // Neutralise spreadsheet formula injection.
        if (!text.isEmpty() && "=+-@\t\r".indexOf(text.charAt(0)) >= 0 && !(value instanceof Number)) {
            text = "'" + text;
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
