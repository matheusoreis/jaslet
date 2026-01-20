package jaslet;

import jaslet.data.JasletResult;
import jaslet.data.JasletRow;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.concurrent.CompletableFuture;

class JasletTest {
    private static final String TEST_DB = "test.db";
    private Jaslet jaslet;

    @BeforeEach
    void setUp() {
        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        jaslet = new Jaslet(TEST_DB);
    }

    @AfterEach
    void tearDown() {
        if (jaslet != null) {
            jaslet.close();
        }

        File dbFile = new File(TEST_DB);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    @DisplayName("Deve criar tabela com sucesso")
    void testCreateTable() throws Exception {
        String sql = "CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)";

        CompletableFuture<JasletResult> future = jaslet.execute(sql, null);
        JasletResult result = future.get();

        assertNotNull(result);
        assertEquals(0, result.affectedRows());
        assertTrue(result.rows().isEmpty());
    }

    @Test
    @DisplayName("Deve inserir dados com sucesso")
    void testInsert() throws Exception {
        jaslet.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)", null).get();

        String sql = "INSERT INTO users (name, age) VALUES (?, ?)";
        Object[] params = { "Alice", 30 };

        CompletableFuture<JasletResult> future = jaslet.execute(sql, params);
        JasletResult result = future.get();

        assertNotNull(result);
        assertEquals(1, result.affectedRows());
    }

    @Test
    @DisplayName("Deve consultar dados com sucesso")
    void testQuery() throws Exception {
        jaslet.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)", null).get();
        jaslet.execute("INSERT INTO users (name, age) VALUES (?, ?)", new Object[] { "Alice", 30 }).get();
        jaslet.execute("INSERT INTO users (name, age) VALUES (?, ?)", new Object[] { "Bob", 25 }).get();

        String sql = "SELECT * FROM users ORDER BY name";
        CompletableFuture<JasletResult> future = jaslet.query(sql, null);
        JasletResult result = future.get();

        assertNotNull(result);
        assertEquals(2, result.affectedRows());
        assertEquals(2, result.rows().size());

        JasletRow firstRow = result.rows().get(0);
        assertEquals("Alice", firstRow.get("name"));
        assertEquals(30, firstRow.get("age"));

        JasletRow secondRow = result.rows().get(1);
        assertEquals("Bob", secondRow.get("name"));
        assertEquals(25, secondRow.get("age"));
    }

    @Test
    @DisplayName("Deve consultar com parâmetros")
    void testQueryWithParams() throws Exception {
        jaslet.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)", null).get();
        jaslet.execute("INSERT INTO users (name, age) VALUES (?, ?)", new Object[] { "Alice", 30 }).get();
        jaslet.execute("INSERT INTO users (name, age) VALUES (?, ?)", new Object[] { "Bob", 25 }).get();

        String sql = "SELECT * FROM users WHERE age > ?";
        Object[] params = { 26 };

        CompletableFuture<JasletResult> future = jaslet.query(sql, params);
        JasletResult result = future.get();

        assertEquals(1, result.rows().size());
        assertEquals("Alice", result.rows().get(0).get("name"));
    }

    @Test
    @DisplayName("Deve atualizar dados com sucesso")
    void testUpdate() throws Exception {
        jaslet.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)", null).get();
        jaslet.execute("INSERT INTO users (name, age) VALUES (?, ?)", new Object[] { "Alice", 30 }).get();

        String sql = "UPDATE users SET age = ? WHERE name = ?";
        Object[] params = { 31, "Alice" };

        CompletableFuture<JasletResult> future = jaslet.execute(sql, params);
        JasletResult result = future.get();

        assertEquals(1, result.affectedRows());

        JasletResult queryResult = jaslet.query("SELECT age FROM users WHERE name = 'Alice'", null).get();
        assertEquals(31, queryResult.rows().get(0).get("age"));
    }

    @Test
    @DisplayName("Deve deletar dados com sucesso")
    void testDelete() throws Exception {
        jaslet.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)", null).get();
        jaslet.execute("INSERT INTO users (name, age) VALUES (?, ?)", new Object[] { "Alice", 30 }).get();
        jaslet.execute("INSERT INTO users (name, age) VALUES (?, ?)", new Object[] { "Bob", 25 }).get();

        String sql = "DELETE FROM users WHERE name = ?";
        Object[] params = { "Alice" };

        CompletableFuture<JasletResult> future = jaslet.execute(sql, params);
        JasletResult result = future.get();

        assertEquals(1, result.affectedRows());

        JasletResult queryResult = jaslet.query("SELECT * FROM users", null).get();
        assertEquals(1, queryResult.rows().size());
        assertEquals("Bob", queryResult.rows().get(0).get("name"));
    }

    @Test
    @DisplayName("Deve retornar lista vazia para query sem resultados")
    void testQueryEmpty() throws Exception {
        jaslet.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)", null).get();

        JasletResult result = jaslet.query("SELECT * FROM users", null).get();

        assertNotNull(result);
        assertTrue(result.rows().isEmpty());
        assertEquals(0, result.affectedRows());
    }

    @Test
    @DisplayName("Deve lançar exceção para SQL inválido")
    void testInvalidSQL() {
        CompletableFuture<JasletResult> future = jaslet.execute("INVALID SQL", null);

        assertThrows(Exception.class, () -> future.get());
    }

    @Test
    @DisplayName("Deve tratar valores nulos")
    void testNullValues() throws Exception {
        jaslet.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)", null).get();
        jaslet.execute("INSERT INTO users (name, age) VALUES (?, ?)", new Object[] { "Alice", null }).get();

        JasletResult result = jaslet.query("SELECT * FROM users", null).get();

        assertEquals(1, result.rows().size());
        assertEquals("Alice", result.rows().get(0).get("name"));
        assertNull(result.rows().get(0).get("age"));
    }

    @Test
    @DisplayName("Deve executar múltiplas operações assíncronas")
    void testMultipleAsyncOperations() throws Exception {
        jaslet.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)", null).get();

        CompletableFuture<JasletResult> insert1 = jaslet.execute("INSERT INTO users (name, age) VALUES (?, ?)",
                new Object[] { "Alice", 30 });
        CompletableFuture<JasletResult> insert2 = jaslet.execute("INSERT INTO users (name, age) VALUES (?, ?)",
                new Object[] { "Bob", 25 });
        CompletableFuture<JasletResult> insert3 = jaslet.execute("INSERT INTO users (name, age) VALUES (?, ?)",
                new Object[] { "Carol", 35 });

        CompletableFuture.allOf(insert1, insert2, insert3).get();

        JasletResult result = jaslet.query("SELECT * FROM users", null).get();
        assertEquals(3, result.rows().size());
    }
}
