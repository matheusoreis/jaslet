package jaslet;

import jaslet.data.JasletResult;
import jaslet.data.JasletRow;
import jaslet.exception.JasletException;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

public class Jaslet {
    private final ExecutorService executor;
    private final Connection connection;

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

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new JasletException("Jaslet: Falha ao fechar banco", e);
        }

        executor.shutdown();
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
