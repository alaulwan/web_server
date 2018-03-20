package httpServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
	public static final int BUFSIZE = 1024;
	public static final int MYPORT = 8888;

	public static void main(String[] args) throws IOException {
		/* Create Server Socket */
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(MYPORT);
		System.out.println("TCP Server is Running on the port: " + MYPORT + ", Buffer Size: " + BUFSIZE);
		while (true)
		 {
			/* Create Socket and wait for a client */
			Socket socket = serverSocket.accept();

			/* Create a thread for each client */
			HttpServerThread httpServerThread = new HttpServerThread(socket, BUFSIZE);
			httpServerThread.start();
		}
	}

}
