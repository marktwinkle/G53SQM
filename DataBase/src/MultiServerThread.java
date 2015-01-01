import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class MultiServerThread extends Thread {

	private final String OK = "+OK ";
	private final String WELCOMING_MESSAGE = "POP3 server ready";
	private final String CLIENT_TERMINATED_MESSAGE = "Connection Terminated by Client";
	private final String SERVER_TIMEOUT_MESSAGE = "Server timeout expired";
	private final boolean AUTO_FLUSH = true;
	private String request, response;
	private int timeout;
	private boolean running;
	private Socket connectionSocket;
	private PrintWriter output;
	private BufferedReader input; 
	private CommandInterpreter cmd;

	public MultiServerThread(Socket socket, int timeout) {
		this.connectionSocket = socket;
		this.timeout = timeout;

		running = true;
		cmd = new CommandInterpreter();
		try {
			setupStreams();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public void run() {	

		try {
			connectionSocket.setSoTimeout(timeout);
			interaction();
		} catch (SocketException socketExpcetion) {
			System.err.println(SERVER_TIMEOUT_MESSAGE);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			endOfConnection();
		}
	}

	/**
	 *  creating send/receive streams
	 * @throws IOException
	 */
	private void setupStreams() throws IOException {
		output = new PrintWriter(connectionSocket.getOutputStream(), AUTO_FLUSH);
		input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
	}

	/** 
	 * start of server/client interaction
	 * @throws IOException
	 */
	private void interaction() throws IOException {
		response = OK+WELCOMING_MESSAGE;
		output.println(response);
		while ((request = input.readLine()) != null) {
			try {	
				response = cmd.handleInput(request);		//sends request to CommandInterpreter
				output.println(response);					//response is sent back to Client
															//CommandInterpreter asks Database for data, and Database responses											
															//CommandInterpreter use Database response to generate 'response', and send it back to Server
			} catch (NullPointerException nullPointerException) {
				System.err.println(CLIENT_TERMINATED_MESSAGE);
				running = false;
			}
		}
	}

	/**
	 * Closes open streams and socket
	 */
	private void endOfConnection() {
		try {
			input.close();
			output.close();
			connectionSocket.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

	}
}
