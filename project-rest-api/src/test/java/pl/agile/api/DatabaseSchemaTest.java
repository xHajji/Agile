package pl.agile.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Run against a dedicated, empty PostgreSQL database; every test rolls back its DDL and data.
@EnabledIfEnvironmentVariable(named = "AGILE_TEST_DATABASE_URL", matches = ".+")
class DatabaseSchemaTest {
    private Connection connection;

    @BeforeEach
    void initializeSchema() throws SQLException {
        connection = DriverManager.getConnection(System.getenv("AGILE_TEST_DATABASE_URL"),
                System.getenv("AGILE_TEST_DATABASE_USER"), System.getenv("AGILE_TEST_DATABASE_PASSWORD"));
        connection.setAutoCommit(false);
        ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
    }

    @AfterEach
    void rollback() throws SQLException {
        if (connection != null) {
            try {
                connection.rollback();
            } finally {
                connection.close();
            }
        }
    }

    @Test
    void createsRequestedTablesAndCanBeInitializedTwice() throws SQLException {
        ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
        try (var statement = connection.createStatement();
             var rows = statement.executeQuery("""
                     SELECT count(*) FROM information_schema.tables
                     WHERE table_schema = 'public'
                       AND table_name IN ('roles', 'app_users', 'statuses', 'projects', 'tasks', 'kanban')
                     """)) {
            assertTrue(rows.next());
            assertEquals(6, rows.getInt(1));
        }
    }

    @Test
    void savesProjectTaskAndKanbanWithGeneratedIdsAndTimestamps() throws SQLException {
        try (var statement = connection.createStatement();
             var rows = statement.executeQuery("""
                     WITH new_project AS (
                         INSERT INTO projects (id_owner, tytul)
                         VALUES ('00000000-0000-0000-0000-000000000001', 'Test schematu') RETURNING id, data_utw
                     ), new_task AS (
                         INSERT INTO tasks (id_projekt, nazwa, id_status)
                         SELECT id, 'Zadanie', (SELECT id FROM statuses WHERE nazwa = 'Do zrobienia')
                         FROM new_project RETURNING id, data_utw, id_status
                     )
                     INSERT INTO kanban (id_task, id_status)
                     SELECT id, id_status FROM new_task RETURNING id, id_task
                     """)) {
            assertTrue(rows.next());
            assertNotNull(rows.getObject("id", UUID.class));
            assertNotNull(rows.getObject("id_task", UUID.class));
            assertFalse(rows.next());
        }
        try (var statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT data_utw FROM tasks")) {
            assertTrue(rows.next());
            assertNotNull(rows.getTimestamp(1));
        }
    }

    @Test
    void rejectsTaskWithoutExistingProject() {
        assertEquals("23503", assertThrows(SQLException.class, () -> {
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("""
                        INSERT INTO tasks (id_projekt, nazwa)
                        VALUES ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Osierocone zadanie')
                        """);
            }
        }).getSQLState());
    }

    @Test
    void rejectsProfileWithoutAuthUser() {
        assertEquals("23503", assertThrows(SQLException.class, () -> {
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("""
                        INSERT INTO app_users (id, email, haslo)
                        VALUES ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'test@example.invalid', NULL)
                        """);
            }
        }).getSQLState());
    }

    @Test
    void requiresProjectOwner() {
        assertEquals("23502", assertThrows(SQLException.class, () -> {
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO projects (tytul) VALUES ('Bez właściciela')");
            }
        }).getSQLState());
    }
}
