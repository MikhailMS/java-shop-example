package com.molotkov;

import com.zaxxer.hikari.HikariDataSource;
import javafx.stage.Stage;
import org.junit.ClassRule;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testfx.framework.junit.ApplicationTest;

import java.sql.SQLException;

public class MainScreenClientTest extends ApplicationTest {
    private HikariDataSource dataSource;
    private static boolean setupIsDone = false;

    @ClassRule
    public static PostgreSQLContainer postgres = new PostgreSQLContainer();

    @Override
    public void start(final Stage stage) throws SQLException {

    }
}
