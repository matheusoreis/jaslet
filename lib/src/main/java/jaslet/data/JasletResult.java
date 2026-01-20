package jaslet.data;

import java.util.List;
import java.util.Optional;

/**
 * Result of a SQL operation executed by Jaslet.
 * <p>
 * Contains the returned rows (for SELECT) and the number of affected rows
 * (for INSERT, UPDATE, DELETE).
 * </p>
 * 
 * @param rows         list of rows returned by the query
 * @param affectedRows number of rows affected by the operation
 * 
 * @since 1.0
 */
public record JasletResult(List<JasletRow> rows, int affectedRows) {
	/**
	 * Checks if the result contains no rows.
	 * 
	 * @return true if there are no rows, false otherwise
	 */
	public boolean isEmpty() {
		return rows.isEmpty();
	}

	/**
	 * Returns the number of rows in the result.
	 * 
	 * @return number of rows
	 */
	public int rowCount() {
		return rows.size();
	}

	/**
	 * Returns the first row of the result, if it exists.
	 * 
	 * @return Optional containing the first row, or empty if there are no rows
	 */
	public Optional<JasletRow> first() {
		if (rows.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(rows.get(0));
	}
}