package com.folio.common.persistence;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Loads the demo library into an empty database. It never runs against a catalogue that already has books,
 * so real data is never touched. Disable with {@code FOLIO_DEMO_DATA=false}.
 */
@Component
@ConditionalOnProperty(name = "folio.demo-data", havingValue = "true")
class DemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;

    DemoDataLoader(DataSource dataSource, JdbcTemplate jdbc, TransactionTemplate tx) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
        this.tx = tx;
    }

    @Override
    public void run(ApplicationArguments args) {
        tx.executeWithoutResult(status -> {
            jdbc.execute("LOCK TABLE books IN SHARE ROW EXCLUSIVE MODE");
            Long books = jdbc.queryForObject("SELECT count(*) FROM books", Long.class);
            if (books != null && books > 0) {
                return;
            }
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("db/demo/demo-data.sql"));
            populator.setSeparator(ScriptUtils.EOF_STATEMENT_SEPARATOR);
            populator.setSqlScriptEncoding("UTF-8");
            populator.populate(DataSourceUtils.getConnection(dataSource));
            log.info("Loaded demo library data");
        });
    }
}
