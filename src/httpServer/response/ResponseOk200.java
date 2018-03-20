package httpServer.response;

import java.io.File;

public class ResponseOk200 extends Response {
	public ResponseOk200(File file) {
		super("200 OK", "", file);
		
	}

}
