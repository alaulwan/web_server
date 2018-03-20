package httpServer.exceptions;

public class CreatedException extends Exception {
	/**
	 *  Created 201.
	 */
	private static final long serialVersionUID = 1L;
	public String location;
	public CreatedException (String location) {
		this.location= location;
	}
}
