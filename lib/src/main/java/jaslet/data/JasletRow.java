package jaslet.data;

import java.util.Map;
import java.util.Set;

public record JasletRow(Map<String, Object> columns) {
	public Object get(String columName) {
		return columns.get(columName);
	}

	public String getString(String columName) {
		Object value = columns.get(columName);

		if (!(value instanceof String)) {
			return null;
		}

		return (String) value;
	}

	public Number getNumber(String columName) {
		Object value = columns.get(columName);

		if (!(value instanceof Number)) {
			return null;
		}

		return (Number) value;
	}

	public Boolean getBoolean(String columnName) {
		Object value = columns.get(columnName);

		if (!(value instanceof Boolean)) {
			return null;
		}

		return (Boolean) value;
	}

	public Set<String> getColumnNames() {
		return columns.keySet();
	}

	public boolean hasColumn(String columnName) {
		return columns.containsKey(columnName);
	}
}
