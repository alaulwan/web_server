package httpServer;

import java.io.File;

public class Request {
	public String recievedMessage;
	boolean isHTTP1_0OR1_1 = false;
	public String header;
	int bodyLenhth;
	private String body;
	private String lineSeparator;
	private String[] recievedMessageLines;

	public enum RequestType {
		GET, POST, PUT
	}

	private RequestType requestType;
	private String orginalRequestPath;
	private String path;
	private File parentFolder;
	private String recievedFileName;
	private String recievedFilecontent;

	public String getRecievedFilecontent() {
		return recievedFilecontent;
	}

	public Request(String recievedMessage) {
		lineSeparator = System.getProperty("line.separator");
		this.recievedMessage = recievedMessage;
		recievedMessageLines = getRecievedMessageLines(recievedMessage);
		path = "src" + File.separator + "httpServer" + File.separator + "resources";
		parentFolder = new File(path);
		parseRequest();
	}

	// divide the received message from the client into lines
	private String[] getRecievedMessageLines(String recievedMessage) {
		String lineSeparator = System.getProperty("line.separator");
		return recievedMessage.split(lineSeparator);
	}

	// Form the request from the received message
	private void parseRequest() {
		this.isHTTP1_0OR1_1 = parseHttpVersion();
		if (!isHTTP1_0OR1_1)
			return;
		this.requestType = parseRequestType();
		this.path = parsepath();
		this.header = parseHeader();
		this.bodyLenhth = parsebodyLenhth();
		if (this.bodyLenhth > 0) {
			this.body = parseBody();
			if (requestType == RequestType.POST || requestType == RequestType.PUT) {
				parseRecievedFile();
			}

		}

	}

	// Separate the received file name from the content
	private void parseRecievedFile() {
		recievedFileName = this.body.split("=", 2)[0];
		recievedFilecontent = this.body.split(",", 2)[1];
	}

	// Form the request's body from a received message from the client
	private String parseBody() {
		StringBuilder bodyBuilder = new StringBuilder();

		/*
		 * Replace each string "%dd" (where d is a digit) with the corresponding
		 * character in ASCII
		 */
		for (int i = (this.recievedMessage.length() - bodyLenhth); i < recievedMessage.length(); i++) {
			char ch = recievedMessage.charAt(i);
			if (ch == '%') {
				ch = (char) Integer.parseInt("" + recievedMessage.charAt(i + 1) + "" + recievedMessage.charAt(i + 2),
						16);
				i += 2;
			}
			bodyBuilder.append(ch);
		}
		return bodyBuilder.toString();

	}

	// Check if the request is HTTP (1.0 or 1.1)
	private boolean parseHttpVersion() {
		boolean isHTTP1_0OR1_1 = false;
		String firstLine = recievedMessageLines[0];
		try {
			String thirdWord = firstLine.split(" ")[2];
			if (thirdWord.equals("HTTP/1.0") || thirdWord.equals("HTTP/1.1"))
				isHTTP1_0OR1_1 = true;
		} catch (ArrayIndexOutOfBoundsException e) {
			isHTTP1_0OR1_1 = false;
		}
		return isHTTP1_0OR1_1;
	}

	// Extract and return the length of the request's body from the header
	private int parsebodyLenhth() {
		int bodyLenhth = 0;
		for (String line : recievedMessageLines) {
			if (line.isEmpty() || line == null || line.equals(lineSeparator))
				break;
			if (line.startsWith("Content-Length")) {
				bodyLenhth = Integer.parseInt(line.substring(16));
				break;
			}
		}
		return bodyLenhth;
	}

	// Form the request's header from a received message from the client
	private String parseHeader() {
		StringBuilder header = new StringBuilder();
		for (String line : recievedMessageLines) {
			if (line.isEmpty() || line == null || line.equals(lineSeparator))
				break;
			header.append(line + lineSeparator);
		}
		return header.toString();
	}

	/*
	 * Extract and return the method-type from the received message For PUT method,
	 * the method type is already assigned while parsing the PATH. Therefore, if the
	 * method type has already PUT value, then leave it PUT
	 */
	private RequestType parseRequestType() {
		if (this.requestType == RequestType.PUT)
			return RequestType.PUT;

		String firstLine = recievedMessageLines[0];
		String firstWord = firstLine.split(" ")[0];
		for (RequestType requestType : RequestType.values()) {
			if (firstWord.equals(requestType.name()))
				return requestType;
		}
		return null;
	}

	// Form the request's path from a received message from the client
	private String parsepath() {
		String path = "src" + File.separator + "httpServer" + File.separator + "resources";
		String firstLine = recievedMessage.split(lineSeparator, 1)[0];
		String requiredPath = firstLine.split(" ", 3)[1];

		// Check if the method type is PUT
		if (requiredPath.startsWith("/_PUT")) {
			this.requestType = RequestType.PUT;
			requiredPath = requiredPath.substring(5);
		}
		this.orginalRequestPath = path + requiredPath;

		if (requiredPath.equals("/")) {
			requiredPath += "index.htm";
		}
		if (requiredPath.endsWith("htm") && !isFileExist(requiredPath)) {
			requiredPath += "l";
		} else if (requiredPath.endsWith("html") && !isFileExist(requiredPath)) {
			requiredPath = requiredPath.substring(0, requiredPath.length() - 1);
		} else if (!requiredPath.endsWith("/") && !requiredPath.contains(".")) {
			requiredPath += File.separator;
		}
		requiredPath = requiredPath.replace('/', File.separatorChar);
		path += requiredPath;
		return path;
	}

	// Check if file in a specific folder is exist
	private boolean isFileExist(String path) {
		return new File(parentFolder, path).exists();
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public boolean isHTTP() {
		return isHTTP1_0OR1_1;
	}

	public String getBody() {
		return body;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public String getPath() {
		return path;
	}

	public File getParentFolder() {
		return parentFolder;
	}

	public String getOrginalRequestPath() {
		return orginalRequestPath;
	}

	public String getRecievedFileName() {
		return recievedFileName;
	}
}
