package httpServer.response;

public class ResponseNotFound404 extends Response {
	public ResponseNotFound404(String wrongPath) {
		super("404 Not Found", "We could not find the page you are looking for."+ System.getProperty("line.separator") + wrongPath, null);
		
	}
}
