package com.molotkov;

import com.molotkov.db.DBConnector;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import java.sql.Connection;

public class DBConnectorTest {
    private DBConnector connector;

    @Before
    public void setUp() {
        connector = new DBConnector("jdbc:postgresql://localhost/test_db");
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
