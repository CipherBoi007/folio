package com.folio.common.csv;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CsvWriterTest {

    @Test
    void quotesSeparatorsAndEscapesQuotes() {
        assertThat(CsvWriter.cell("Thinking, Fast and Slow")).isEqualTo("\"Thinking, Fast and Slow\"");
        assertThat(CsvWriter.cell("The \"Guide\"")).isEqualTo("\"The \"\"Guide\"\"\"");
        assertThat(CsvWriter.cell(null)).isEmpty();
        assertThat(CsvWriter.cell(-3)).isEqualTo("-3");
    }

    @Test
    void neutralisesSpreadsheetFormulas() {
        assertThat(CsvWriter.cell("=HYPERLINK(\"x\")")).startsWith("\"'=");
        assertThat(CsvWriter.cell("+91 98430")).isEqualTo("'+91 98430");
    }
}
