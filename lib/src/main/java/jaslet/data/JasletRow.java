package jaslet.data;

import java.util.Map;
import java.util.Set;

/**
 * Represents a result row from a SQL query.
 * <p>
 * Provides access to column values through typed methods
 * that return null if the value cannot be converted to the expected type.
 * </p>
 * 
 * @param columns map of column names to their values
 * 
 * @since 1.0
 */
public record JasletRow(Map<String, Object> columns) {
	/**
	 * Returns the value of a column.
	 * 
	 * @param columName column name
	 * @return column value, or null if it does not exist
	 */
	public Object get(String columName) {
		return columns.get(columName);
	}

	/**
	 * Returns the value of a column as a String.
	 * 
	 * @param columName column name
	 * @return value as String, or null if not a String or does not exist
	 */
	public String getString(String columName) {
		Object value = columns.get(columName);

		if (!(value instanceof String)) {
			return null;
		}

		return (String) value;
	}

	/**
	 * Returns the value of a column as a Number.
	 * 
	 * @param columName column name
	 * @return value as Number, or null if not a Number or does not exist
	 */
	public Number getNumber(String columName) {
		Object value = columns.get(columName);

		if (!(value instanceof Number)) {
			return null;
		}

		return (Number) value;
	}

	/**
	 * Returns the value of a column as a Boolean.
	 * 
	 * @param columnName column name
	 * @return value as Boolean, or null if not a Boolean or does not exist
	 */
	public Boolean getBoolean(String columnName) {
		Object value = columns.get(columnName);

		if (!(value instanceof Boolean)) {
			return null;
		}

		return (Boolean) value;
	}

	/**
	 * Returns the set of all column names.
	 * 
	 * @return set with column names
	 */
	public Set<String> getColumnNames() {
		return columns.keySet();
	}

	/**
	 * Checks if the row contains a specific column.
	 * 
	 * @param columnName column name to check
	 * @return true if the column exists, false otherwise
	 */
	public boolean hasColumn(String columnName) {
		return columns.containsKey(columnName);
	}
}
