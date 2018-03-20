package httpServer.exceptions;

public class FoundException extends Exception {
	/**
	 *  Response 302.
	 */
	private static final long serialVersionUID = 1L;
	public String correctPath;
	public FoundException (String correctPath) {
		this.correctPath= correctPath;
	}
}
