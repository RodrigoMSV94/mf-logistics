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

        assertEquals(5, changeLog.getChangeSets().size());
        assertTrue(changelogContent.contains("73025558Y"));
        assertTrue(changelogContent.contains("SAÚL GUERRERO LÓPEZ"));
        assertTrue(changelogContent.contains("20261030-04-snapshot-invoice-issuer"));
        assertTrue(changelogContent.contains("Y8023499D"));
        assertTrue(changelogContent.contains("<update tableName=\"invoices\">"));
        assertTrue(changelogContent.contains("columnName=\"issuer_name\""));
        assertTrue(changelogContent.contains("20261030-05-email-to-username"));
        assertTrue(changelogContent.contains("newColumnName=\"username\""));
    }
}
