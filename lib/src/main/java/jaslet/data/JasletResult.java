package jaslet.data;

import java.util.List;
import java.util.Optional;

public record JasletResult(List<JasletRow> rows, int affectedRows) {
	public boolean isEmpty() {
		return rows.isEmpty();
	}

	public int rowCount() {
		return rows.size();
	}

	public Optional<JasletRow> first() {
		if (rows.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(rows.get(0));
	}
}