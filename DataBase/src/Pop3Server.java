/*
 * This is the main network section
 * Server.java class is responsible for running the server
 * To run the server, the file is compiled and used in the format: java Server <port number>
 * It opens a server socket for a specific port number (default is 110), and waits for clients to connects
 * It supports multiple clients connections by creating new threads (MultiServerThread.java)
 * This works by opening streams to send and read input and responses
 * The server is responsible for reading a request from a client,then asks CommandInterpreter class for response. The CommandInterpreter will return a response to the server, and then the server redirect it to the client
 * The connection between server and client can be terminated in three ways:
 * 		1. Server Timeout: When the client is not active for SERVER_TIMEOUT time (default is 10 minutes)
 * 		2. Client issuing QUIT command in AUTHORIZATION state
 * 		3. Client issuing QUIT command in UPDATE state 
 * For testing, the 'Client.java' class is created. It connects using the IP and port used for server
 * This method is easy to test multiple connections to server, and verifying multiple creations of CommandInterpreter objects
 */

import java.io.IOException;
import java.net.ServerSocket;

public class Pop3Server {
	public static void main(String[] args) throws IOException {

		// incorrect number of command line arguments
		if (args.length != 2) {
			System.out.println("Usage: java Server <port number> <Server timeout (in seconds)>");
			System.exit(1);
		}

		try {
			int portNumber = Integer.parseInt(args[0]);
			int timeout = Integer.parseInt(args[1])*1000; //in milliseconds
			boolean listening = true;

			// open server socket and wait for client connections
			try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
				while (listening) {
					MultiServerThread server = new MultiServerThread(serverSocket.accept(), timeout);
					server.start();
				}
			// when port is busy
			} catch (IOException ioException) {
				System.err.println("Could not listen on port " + portNumber);
				System.exit(-1);
			}
		// invalid command line arguments 
		} catch (NumberFormatException numberFormatException) {
			System.out.println("Usage: java Server <port number> <Server timeout (in seconds)>");
			System.exit(1);
		}
	}
}