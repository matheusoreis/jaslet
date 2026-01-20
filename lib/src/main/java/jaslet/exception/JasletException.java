package jaslet.exception;

/**
 * Exception thrown when an error occurs during Jaslet operations.
 * <p>
 * This is a RuntimeException, therefore it does not need to be declared or
 * caught
 * explicitly. Typically encapsulates SQL or connection errors.
 * </p>
 * 
 * @since 1.0
 */
public class JasletException extends RuntimeException {
	/**
	 * Creates a new JasletException with the specified message.
	 * 
	 * @param message message describing the error
	 */
	public JasletException(String message) {
		super(message);
	}

	/**
	 * Creates a new JasletException with message and cause.
	 * 
	 * @param message message describing the error
	 * @param cause   original exception that caused the error
	 */
	public JasletException(String message, Throwable cause) {
		super(message, cause);
	}
}
