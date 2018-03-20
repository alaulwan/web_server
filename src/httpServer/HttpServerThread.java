package httpServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import httpServer.response.Response;
import httpServer.response.ResponseFactory;
import httpServer.response.ResponseInternalServerError500;

public class HttpServerThread extends Thread {
	private Socket socket;
	public static int counter = 0;
	private final int clientId;
	private final int BUFSIZE;
	private InputStream inputStream;
	private OutputStream outputStream;

	public HttpServerThread(Socket socket, int BUFSIZE) {
		this.BUFSIZE = BUFSIZE;
		this.socket = socket;
		this.clientId = ++counter;
		try {
			inputStream = new DataInputStream(this.socket.getInputStream());
			outputStream = new DataOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// create an empty message
		String recievedMessage = "";

		int inputStreamAvailability = 0;

		/*
		 * do loop until the receiving of the current message be done (if the server's
		 * buffer-size is small, then we need more loops)
		 */
		do {
			// create buffer
			byte[] buf = new byte[BUFSIZE];

			try {
				// read and save the received message in the buffer
				inputStream.read(buf);
				inputStreamAvailability = inputStream.available();
			} catch (IOException e) {
				closeSoket();
				System.out.println("Error while receiving");
				return;
			}

			// Convert the buffer to string
			String subRecievedMessage = new String(buf);

			/*
			 * If the received part of the current message is the last part, then remove the
			 * empty bytes.
			 */
			if (inputStreamAvailability < 1)
				subRecievedMessage = subRecievedMessage.trim();

			// add the message to recievedMessage
			recievedMessage += subRecievedMessage;
		} while (inputStreamAvailability > 0);

		if (recievedMessage != null && !recievedMessage.isEmpty()) {
			// Create new request according to the received message
			Request recievedRequest = new Request(recievedMessage);

			// Print information about the client and the request's header in the console
			printRequestSummary(recievedRequest.getHeader());

			/*
			 * Create a response factory to generate a suitable response according to the
			 * request
			 */
			ResponseFactory responseFactory = new ResponseFactory(recievedRequest);

			// Generate the response. ()
			Response response = responseFactory.getResponse();

			/*
			 * If the response is null (in case the Response Factory encountered an error 
			 * while generate the response) Then the server will returns the response 500
			 */
			if (response == null)
				response = new ResponseInternalServerError500();

			// Send the response to the client
			response.send(outputStream);
		}

		// close the socket
		closeSoket();

	}

	// Method to print a request-summary
	private void printRequestSummary(String recievedHeader) {
		if (recievedHeader != null && !recievedHeader.isEmpty()) {
			System.out.printf("\n[Client " + clientId + "] TCP echo request from %s",
					socket.getInetAddress().getHostName());
			System.out.printf(" using port %d\n", socket.getPort());
			System.out.println("Recieved(" + recievedHeader.length() + " bytes):\n" + "Header:\n" + recievedHeader);

		}

	}

	// Method to close the socket and print a tip in the console
	private void closeSoket() {
		try {
			socket.close();
			System.out.println("\n******* Client " + clientId + ": connection is closed *******\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
