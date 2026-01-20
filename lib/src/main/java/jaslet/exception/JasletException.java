package jaslet.exception;

public class JasletException extends Exception {
	public JasletException(String message) {
		super(message);
	}

	public JasletException(String message, Throwable cause) {
		super(message, cause);
	}
}
