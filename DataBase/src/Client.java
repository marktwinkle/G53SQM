
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	private final boolean AUTO_FLUSH = true;
	private final String OK = "+OK ";
	private final String ERR = "-ERR ";
	private final String CRLF = "\r\n";
	private final String LOST_CONNECTION_MESSAGE = "Connection to server is lost";
	private final String CLIENT_REQUEST_QUIT = "QUIT";
	private String response;
	private String request;
	private boolean running;
	private Socket connection;
	private PrintWriter serverOutput;
	private BufferedReader serverInput;
	private PrintStream clientOutput;
	private BufferedReader clientInput;
	private BufferedReader clientInputMark;

	private BufferedReader clientInputMarkAndAiman;

	private BufferedReader clientInputAiman;

	private String test="Mark";
	private String viab;

	public Client(String hostName, int portNumber, InputStream clientInput, PrintStream clientOutput) {
		try {
			connection = new Socket(hostName, portNumber);
			setupServerStreams();
			setupClientStreams(clientInput,clientOutput);
		} catch (UnknownHostException unknownHostException) {
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} catch (IOException ioException) {
			System.err.println("Couldn't get I/O for the connection to " + hostName);
			System.exit(1);
		}
		running = true;
	}

	public Client(String hostName, int portNumber) {
		this(hostName, portNumber, System.in, System.out);
	}


	public void startRunning() {
		try {
			interaction();
			endOfConnection();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	/**
	 * start of client/server interaction
	 * @throws IOException
	 */
	private void interaction() throws IOException {
		response = serverInput.readLine();
		clientOutput.println(response);
		while (running) {
			request = clientInput.readLine();				
			response = handleRequest(request);

			if (response != (null)) {
				clientOutput.print(response);
				
				//handling terminating cases
				if ((request.equals(CLIENT_REQUEST_QUIT) && (response.split(" ")[0].equals(OK.trim()) || response.split(" ")[0].equals(ERR.trim())))) {
					running = false;
				}
			//handling server timeout
			} else {
				System.err.println(LOST_CONNECTION_MESSAGE);
				running = false;
			}
		}
	}

	/**
	 * Closes socket and open streams
	 */
	private void endOfConnection() {
		try {
			connection.close();
			serverOutput.close();
			serverInput.close();
			clientInput.close();
		} catch (IOException ioException){
			ioException.printStackTrace();
		}
	}

	/**
	 *  this function will initialize client input and output streams from values in constructor
	 * @param clientInput
	 * @param clientOutput
	 */
	private void setupClientStreams(InputStream clientInput, PrintStream clientOutput) {
		this.clientInput = new BufferedReader(new InputStreamReader(clientInput));
		this.clientOutput = clientOutput;
	}

	/**
	 * this function will initialize server input and output from values in constructor
	 * @throws IOException
	 */
	private void setupServerStreams() throws IOException {	
		serverOutput = new PrintWriter(connection.getOutputStream(), AUTO_FLUSH);
		serverInput = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	}

	/**
	 * handles request and forward it to Server, then returns server's response 
	 * @param request from client
	 * @return response from server
	 * @throws InterruptedException 
	 */
	private String handleRequest(String request) {
		serverOutput.println(request);
		
		try {
			String serverResponse = "";
			Thread.sleep(250);
			if (serverInput.ready()) {
				while (serverInput.ready()) {
					serverResponse += serverInput.readLine()+CRLF;
				}
				return serverResponse;
			}
			
		} catch (IOException ioException) {
			
		} catch (InterruptedException interruptedException) {
			
		}
		return null;
	}
	
}
