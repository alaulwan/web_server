package httpServer.response;


public class ResponseCreated201 extends Response {
	public ResponseCreated201(String Location) {
		super("201 Created"+System.getProperty("line.separator") + "Location: "+ Location, "The file "+ Location +" created", null);
	}
}
