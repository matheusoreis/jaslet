package jaslet;

import jaslet.data.JasletResult;
import jaslet.data.JasletRow;
import jaslet.exception.JasletException;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Asynchronous client for interacting with SQLite databases.
 * <p>
 * All SQL operations are executed in a non-blocking manner through
 * {@link CompletableFuture}, allowing the application to remain responsive
 * while waiting for database results.
 * </p>
 * 
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>{@code
 * Jaslet db = new Jaslet("my-database.db");
 * 
 * db.query("SELECT * FROM users WHERE age > ?", new Object[] { 18 })
 *         .thenAccept(result -> {
 *             result.rows().forEach(row -> System.out.println(row.get("name")));
 *         });
 * 
 * db.close();
 * }</pre>
 * 
 * @author Matheus R. Oliveira
 * @version 1.0
 * @since 1.0
 */
public class Jaslet {
    private final ExecutorService executor;
    private final Connection connection;

    /**
     * Creates a new Jaslet instance connected to the specified SQLite database.
     * <p>
     * If the file does not exist, it will be created automatically. All SQL
     * operations are executed in a dedicated thread to ensure thread-safety.
     * </p>
     * 
     * @param path path to the SQLite database file
     * @throws JasletException if there is an error connecting to the database
     */
    public Jaslet(String path) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch (Exception e) {
            throw new JasletException("Jaslet: Falha ao iniciar banco " + path, e);
        }

        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("JasletExecutor");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Executes a SQL SELECT query asynchronously.
     * <p>
     * This method returns immediately with a {@link CompletableFuture} that will
     * be completed when the query finishes executing. Use this method for
     * queries that return data (SELECT).
     * </p>
     * 
     * @param sql    SQL command to execute (use ? for parameters)
     * @param params array of parameters to replace the ? in the SQL, or null if no
     *               parameters
     * @return CompletableFuture that will be completed with the query result
     * @throws JasletException (asynchronously) if there is an error executing the
     *                         SQL
     * 
     * @see #execute(String, Object[]) for commands that modify data
     */
    public CompletableFuture<JasletResult> query(String sql, Object[] params) {
        return CompletableFuture.supplyAsync(() -> querySync(sql, params), executor);
    }

    /**
     * Executes a SQL modification command asynchronously.
     * <p>
     * Use this method for INSERT, UPDATE, DELETE, CREATE TABLE and other
     * commands that modify the database. The result contains the number
     * of affected rows.
     * </p>
     * 
     * @param sql    SQL command to execute (use ? for parameters)
     * @param params array of parameters to replace the ? in the SQL, or null if no
     *               parameters
     * @return CompletableFuture that will be completed with the execution result
     * @throws JasletException (asynchronously) if there is an error executing the
     *                         SQL
     * 
     * @see #query(String, Object[]) for SELECT queries
     */
    public CompletableFuture<JasletResult> execute(String sql, Object[] params) {
        return CompletableFuture.supplyAsync(() -> executeSync(sql, params), executor);
    }

    /**
     * Closes the database connection and shuts down the thread executor.
     * <p>
     * This method should be called when you finish using Jaslet to
     * properly release resources. Pending operations will be completed before
     * shutdown.
     * </p>
     * 
     * @throws JasletException if there is an error closing the connection
     */
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new JasletException("Jaslet: Falha ao fechar banco", e);
        }

        executor.shutdown();
    }

    private JasletResult querySync(String sql, Object[] params) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            bindParameters(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                return parseResultSet(rs);
            }
        } catch (SQLException e) {
            throw new JasletException("Jaslet: Falha ao executar query", e);
        }
    }

    private JasletResult executeSync(String sql, Object[] params) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            bindParameters(stmt, params);
            int affectedRows = stmt.executeUpdate();
            return new JasletResult(Collections.emptyList(), affectedRows);
        } catch (SQLException e) {
            throw new JasletException("Jaslet: Falha ao executar comando", e);
        }
    }

    private void bindParameters(PreparedStatement stmt, Object[] params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }

    private JasletResult parseResultSet(ResultSet rs) throws SQLException {
        List<JasletRow> rows = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> columns = new HashMap<>();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object value = rs.getObject(i);

                columns.put(columnName, value);
            }

            rows.add(new JasletRow(columns));
        }

        return new JasletResult(rows, rows.size());
    }
}
