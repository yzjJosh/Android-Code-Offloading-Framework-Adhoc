package mobilecloud.server;

public class IllegalResponseException extends Exception {
	private static final long serialVersionUID = 1L;

	public IllegalResponseException() {
		super();
	}

	public IllegalResponseException(String message) {
		super(message);
	}
}
