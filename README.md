# Jaslet

Asynchronous SQLite interface for Java. All operations return `CompletableFuture` and execute in a dedicated thread.

## Installation

```gradle
repositories {
    maven { url = 'https://jitpack.io' }
}

dependencies {
    implementation 'io.github.matheusoreis:jaslet:1.0.2'
}
```

## Quick Start

```java
Jaslet db = new Jaslet("users.db");

// Create table
db.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER)", null)
    .thenAccept(r -> System.out.println("Table created"));

// Insert
db.execute("INSERT INTO users (name, age) VALUES (?, ?)", new Object[]{"Alice", 30})
    .thenAccept(r -> System.out.println("Inserted"));

// Query
db.query("SELECT * FROM users WHERE age > ?", new Object[]{20})
    .thenAccept(result -> {
        for (JasletRow row : result.rows()) {
            System.out.println(row.getString("name") + ": " + row.getNumber("age"));
        }
    });

db.close();
```

## Usage

### Non-Blocking

```java
db.query("SELECT * FROM users", null)
    .thenAccept(result -> {
        System.out.println("Found " + result.rowCount() + " rows");
    });
```

### Blocking

```java
JasletResult result = db.query("SELECT * FROM users", null).get();
```

### Error Handling

```java
db.execute("INSERT INTO users (name) VALUES (?)", new Object[]{"Bob"})
    .exceptionally(e -> {
        System.err.println("Error: " + e.getMessage());
        return null;
    });
```

### Chaining Operations

```java
db.query("SELECT id FROM users WHERE name = ?", new Object[]{"Alice"})
    .thenCompose(result -> {
        int id = result.first().get().getNumber("id").intValue();
        return db.execute("UPDATE users SET age = ? WHERE id = ?", new Object[]{31, id});
    })
    .thenAccept(r -> System.out.println("Updated"));
```

## API

**Jaslet**
- `query(String sql, Object[] params)` - Execute SELECT
- `execute(String sql, Object[] params)` - Execute INSERT/UPDATE/DELETE/DDL
- `close()` - Close connection

**JasletResult**
- `rows()` - List of rows
- `affectedRows()` - Number of affected rows
- `isEmpty()` - No rows?
- `first()` - Optional first row

**JasletRow**
- `get(String col)` - Raw value
- `getString(String col)` - As String
- `getNumber(String col)` - As Number
- `getBoolean(String col)` - As Boolean
- `hasColumn(String col)` - Column exists?

## Notes

- Thread-safe: use one instance across multiple threads
- Operations are serialized automatically
- Always use parameterized queries (`?`) to prevent SQL injection
- Call `close()` when done to release resources

## License

MIT
