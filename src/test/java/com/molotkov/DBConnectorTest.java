package com.molotkov;

import com.molotkov.db.DBConnector;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import java.sql.Connection;
import com.zaxxer.hikari.HikariConfig;

import org.junit.ClassRule;
import org.testcontainers.containers.PostgreSQLContainer;

public class DBConnectorTest {
    private DBConnector connector;
    private HikariConfig hikariConfig;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Before
    public void setUp() {
        hikariConfig = new HikariConfig();
        connector = new DBConnector(postgres.getJdbcUrl());
    }

    @Test
    public void testDBConnectorConstructor() {
        assertTrue(connector instanceof DBConnector);
    }

    @Test
    public void testChangeDBUrl() {
        connector.changeDBUrl("new url");
        assertTrue(connector.getDbUrl().equals("new url"));
    }

    @Test
    public void testGetConnection() {
        assertTrue(connector.getConnection() instanceof Connection);
    }
}
