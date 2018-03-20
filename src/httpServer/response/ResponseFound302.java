package httpServer.response;

public class ResponseFound302 extends Response {

	public ResponseFound302(String correctPath) {
		super("302 Found"+System.getProperty("line.separator") + "Location: "+ correctPath, "The server found The page you are looking for at the following path:\n"+ correctPath, null);
	}

}
