package httpServer.exceptions;

public class NotFoundException extends Exception {
	/**
	 *  Response 404.
	 */
	private static final long serialVersionUID = 1L;
	public String wrongPath;
	
	public NotFoundException(String wrongPath) {
		this.wrongPath = wrongPath;
	}

}
