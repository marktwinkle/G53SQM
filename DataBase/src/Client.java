
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
	private final String CLIENT_REQUEST_QUIT = "QUIT";
	private final String SERVER_RESPONSE_THREAD_NAME = "ServerResponseThread";
	private String response;
	private String request;
	private Socket connection;
	private PrintWriter serverOutput;
	private BufferedReader serverInput;
	private PrintStream clientOutput;
	private BufferedReader clientInput;
	private boolean running;


	public Client(String hostName, int portNumber, InputStream clientInput, PrintStream clientOutput) {
		try {
			connection = new Socket(hostName, portNumber);
			setupServerStreams();
			setupClientStreams(clientInput,clientOutput);
		} catch (UnknownHostException unknownHostException) {
			System.err.println("Don't know about host " + hostName);
			unknownHostException.printStackTrace();
			System.exit(1);
		} catch (IOException ioException) {
			System.err.println("Couldn't get I/O for the connection to " + hostName);
			ioException.printStackTrace();
			System.exit(1);
		}
	}

	public Client(String hostName, int portNumber) {
		this(hostName, portNumber, System.in, System.out);
	}

	public void runClient() {
		try {
			interaction();
			endOfConnection();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	/**
	 * start of client/server interaction
	 * reads request and forwards it to ServerThread, then returns serverThread's response 
	 * <b>request</b> request from client input [what he typed]
	 * <b>response</b> the response from serverThread to the client request 
	 * @throws IOException 
	 */
	private void interaction() throws IOException {
		Thread responseThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!Thread.interrupted()) {
                	try {
                    	while ((response = serverInput.readLine()) != null) {
                			clientOutput.println(response);
                		}
					} catch (IOException e) {
						e.printStackTrace();
					}
                }
            }
        }, SERVER_RESPONSE_THREAD_NAME);
        responseThread.start();
        
        running = true;
		while (running) {
			request = clientInput.readLine();
			if (request != null) {
				//send the user command to server
				serverOutput.println(request);
				if (request.equals(CLIENT_REQUEST_QUIT)) {
					running = false;
					responseThread.interrupt();
				}
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
}
