package httpServer.response;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public abstract class Response {
	private String response;
	private String header;
	private String body;
	private final String htmlExtention = "html";
	private final String httpVersion = "HTTP/1.1 ";
	private File file;
	private String lineSeparator;

	private enum DataType {
		text_html("html,htm"), text_css("css"), text_javascript("js"), image_png("png"), image_gif("gif"), image_jpeg(
				"jpg,jpeg"), application_unknown("*");
		private List<String> extensions;
		private DataType(String extensions) {
			this.extensions = Arrays.asList(extensions.split(","));
		}
	}

	public Response(String Header, String Content, File file) {
		lineSeparator = System.getProperty("line.separator");
		this.file = file;
		this.header = httpVersion + Header + lineSeparator;
		this.body = "<html><body><h1>" + Header + "</h1><p>" + Content + "</p></body></html>";

	}

	// Form the response's header
	protected void parseHeader(long dataLong, String fileExtension) {
		header += "Date: " + new Date().toString() + lineSeparator;
		header += "Content-Length: " + dataLong + lineSeparator;
		header += "Content-Type: " + getType(fileExtension) + lineSeparator + lineSeparator;
	}

	// Send the response to the client
	public void send(OutputStream outStream) {
		/*
		 * If the response contains a file, then form the header according to the
		 * file-length and its extension. Then send the response by the method
		 * sendFile(outStream)
		 */
		if (file != null && file.isFile()) {
			parseHeader(file.length(), file.getName().split("\\.")[file.getName().split("\\.").length - 1]);
			sendFile(outStream);
		}
		/*
		 * Else if the response is a specific HTTP response, then form the header as
		 * html according to the HTTP response code Then send the response by the method
		 * sendContent(outStream)
		 */
		else {
			parseHeader(body.length(), htmlExtention);
			this.response = header + body;
			sendContent(outStream);
		}

	}

	// Method to send a response that contains a file
	private void sendFile(OutputStream outStream) {
		DataOutputStream outputStream = new DataOutputStream(outStream);
		try {
			outputStream.write(this.header.getBytes());
			System.out.println("sent:\n" + this.header);
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] bytes = new byte[1024];
			while (fileInputStream.available() > 0) {
				int readBytes = fileInputStream.read(bytes);
				outStream.write(bytes, 0, readBytes);
			}
			fileInputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// Method to send a specific HTTP response
	public void sendContent(OutputStream outStream) {
		DataOutputStream outputStream = new DataOutputStream(outStream);
		try {
			outputStream.write(this.response.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Define the response's Content-Type according to the file's extension
	public String getType(String fileExtension) {
		for (DataType dataType : DataType.values()) {
			if (dataType.extensions.contains(fileExtension))
				return dataType.toString().replace('_', '/');
		}
		return "application/unknown";
	}

	public String getHeader() {
		return header;
	}

	public String getBody() {
		return body;
	}
}
