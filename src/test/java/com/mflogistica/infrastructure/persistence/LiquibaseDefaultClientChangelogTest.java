package com.mflogistica.infrastructure.persistence;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiquibaseDefaultClientChangelogTest {

    @Test
    void shouldParseChangelogAndSeedDefaultClient() throws Exception {
        ClassLoaderResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance()
                .getParser("db/changelog/db.changelog-master.xml", resourceAccessor)
                .parse("db/changelog/db.changelog-master.xml", new ChangeLogParameters(), resourceAccessor);
        String changelogContent = new ClassPathResource("db/changelog/db.changelog-master.xml")
                .getContentAsString(StandardCharsets.UTF_8);
        String initialSchemaContent = new ClassPathResource(
                "db/changelog/20261125-01-initial-schema.xml")
                .getContentAsString(StandardCharsets.UTF_8);

        assertEquals(2, changeLog.getChangeSets().size());
        assertTrue(changelogContent.contains("20261125-01-initial-schema.xml"));
        assertTrue(initialSchemaContent.contains("20261125-01-create-schema"));
        assertTrue(initialSchemaContent.contains("20261125-02-seed-initial-data"));
        assertTrue(initialSchemaContent.contains("tableName=\"users\""));
        assertTrue(initialSchemaContent.contains("tableName=\"invoices\""));
        assertTrue(initialSchemaContent.contains("tableName=\"invoice_lines\""));
        assertTrue(initialSchemaContent.contains("tableName=\"clients\""));
        assertTrue(initialSchemaContent.contains("tableName=\"fiscal_data\""));
        assertTrue(initialSchemaContent.contains("name=\"taxable\""));
        assertTrue(initialSchemaContent.contains("name=\"issuer_name\""));
        assertTrue(initialSchemaContent.contains("73025558Y"));
        assertTrue(initialSchemaContent.contains("SAÚL GUERRERO LÓPEZ"));
        assertTrue(initialSchemaContent.contains("Y8023499D"));
    }
}
