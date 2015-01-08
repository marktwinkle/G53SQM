


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {
	private final int COMMAND = 0;
	private final int ARG_ONE = 1;
	private final int ARG_TWO = 2;
	private final int ARGUMENT_MAX_LENGTH = 255;
	private final int AUTHORIZATION_STATE = 0;
	private final int TRANSACTION_STATE = 1;
	private final String OK = "+OK ";
	private final String ERR = "-ERR ";
	
	//Error messages
	private final String ERROR_SETUP_STREAMS = "FAILED TO SETUP INPUT/OUTPUT STREAMS";
	private final String ERROR_READ_INPUT = "FAILED TO READ INPUT";
	
	private Server server;
	private Socket connectionSocket;
	private PrintWriter output;
	private BufferedReader input; 
	private boolean running;
	private int state;
	private ClientInfo client;

	public ServerThread(Server server, Socket clientSocket) {
		this.server = server;
		connectionSocket = clientSocket;
		running = true;
		state = 0;
	}

	@Override
	public void run() {

		setupStreams();
		interaction();
		endOfConnection();
	}

	public boolean isRunning() {
		return running;
	}

	public ClientInfo getClient() {
		return client;
	}
	
	public synchronized void messageToClient(String message) {
		output.println(message);
	}
	
	// -------------------- Private Methods --------------------

	/**
	 * Initialize ServerThread input and output streams from connection socket
	 * <b>input</b> reads data coming from the other end of stream to this class
	 * <b>output</b> writes data to the other end of stream
	 */
	private void setupStreams() {
		try {
			input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			output = new PrintWriter(connectionSocket.getOutputStream(), true);
		} catch (IOException e) {
			System.err.println(ERROR_SETUP_STREAMS);
			e.printStackTrace();
		}
	}

	/**
	 * start of client/server interaction
	 * reads requests and forwards them to Server, obtain response from server and then forward it back to client 
	 * <b>request</b> request from client class
	 * <b>response</b> the response from server to the client request 
	 */
	private void interaction() {
		messageToClient("Welcome to the 'PAIR CHAT' application");
		String request, response;
		try {
			while ((request = input.readLine()) != null){
				response = handleInput(request);
				output.println(response);
			}
		} catch (IOException e) {
			System.err.println(ERROR_READ_INPUT);
			e.printStackTrace();
		}
	}

	/**
	 * Closes socket and open streams
	 */
	private void endOfConnection() {
		try {
			connectionSocket.close();
			input.close();
			output.close();
			
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	// ------------ Handling Request ---------------------

	/**
	 * Verify the input against the application state
	 * @param input: message from client
	 * @return response: output of client request
	 */
	private String handleInput(String input) {

		String response;
		switch (state) {

		case AUTHORIZATION_STATE: 
			return authorization(input);

		case TRANSACTION_STATE: 
			return transaction(input);

		default:
			response = "Invalid state NO."+state;
			return ERR+response+" "+input;
		}
	}

	/**
	 * authorization state commands
	 * @param request
	 * @return response
	 */
	private String authorization(String request) {
		String [] inputLine = request.split(" ");
		String response = "Invalid command";

		if (!validCommand(request)) {
			return ERR+response;
		} 

		switch (inputLine [COMMAND]) {

		case "IDEN":
			String username = inputLine[ARG_ONE];
			if (!server.isUserExist(username) && validateUserName(username)) {
				state = TRANSACTION_STATE;
				client = new ClientInfo(username);
				server.addClient(client);
				response = username+". You are now logged in";
				return OK+response;
			}	
			response = inputLine[ARG_ONE]+" invalid username";
			return ERR+response;

		case "QUIT":
			response = "Quitting.";
			return OK+response;

		default: 
			return ERR+response;
		}
	}

	/**
	 * transaction state commands
	 * @param request
	 * @return response
	 */
	private String transaction(String request) {
		String [] inputLine = request.split(" ");
		String response = "Invalid command";
		String message = "";

		if (!validCommand(request)) {
			return ERR+response;
		} 

		switch (inputLine [COMMAND]) {

		case "STAT":
			response = "Number of logged in users: "+ server.getClientsCount()
			+ "\r\nYou have sent (" + client.getMessagesCounter() + ") messages"; 
			return OK+response;

		case "LIST":
			response = "List of logged in users: ";
			response += server.getUsersList();
			return OK+response;	

		case "MESG": {
			response = "User does not exist";
			String destinationUsername = inputLine[ARG_ONE];

			message = client.getUsername()+" --> ";
			//concatenate message
			message += concatenateMessage(ARG_TWO, inputLine);

			//send message
			if(server.messageToUser(message, destinationUsername)) {
				client.increaseMessagesCounter();
				response = "Message sent";
				return OK+response;
			}
			return ERR+response;
		}
		case "HAIL": {
			message = client.getUsername()+" to everyone --> ";
			//concatenate message
			message += concatenateMessage(ARG_ONE, inputLine);
			server.messageToEveryone(message);
			client.increaseMessagesCounter();
			response = "Broadcast message sent";
			return OK+response;
		}
		case "QUIT":
			server.removeClient(client);
			response = "Quitting";
			return OK+response;
		default: 
			return ERR+response;
		}
	}

	/**
	 * Check the input command for existence and valid number of arguments
	 * @param inputLine: user input line
	 * @return True: command exists and valid arguments number provided, False: command doesn't exist or invalid arguments number provided.
	 */
	private boolean validCommand (String request) {
		String [] inputLine = request.split(" ");

		switch (inputLine[COMMAND]) {

		// commands with no arguments
		case "STAT":
		case "QUIT":
		case "LIST":
			if (inputLine.length-1 == COMMAND) {
				return true;
			}
			return false;

			// commands with one argument
		case "IDEN":
			if (inputLine.length-1 == ARG_ONE && inputLine[ARG_ONE].length() <= ARGUMENT_MAX_LENGTH) {
				return true;
			}
			return false;

			// commands with one or multiple argument
		case "HAIL":
			if (inputLine.length-1 >= ARG_ONE && inputLine[ARG_ONE].length() <= ARGUMENT_MAX_LENGTH) {
				return true;
			}
			return false;

			// commands with two arguments
		case "MESG":
			if (inputLine.length-1 >= ARG_TWO && inputLine[ARG_ONE].length() <= ARGUMENT_MAX_LENGTH) {
				return true;
			}
			return false;

		default: 
			return false; 
		}
	}

	/**
	 * Checks user name for illegal naming
	 * @param username: user-input user name
	 * @return True: no invalid characters, False: invalid characters are inputed
	 */
	private boolean validateUserName(String username) {
		//check length
		if (username.length() <= 1) {
			return false;
		} else {
			//check characters
			for (int i = 0 ; i < username.length() ; i++) {
				if (!(
						(username.charAt(i) >= 'a' && username.charAt(i) <= 'z') || 
						(username.charAt(i) >= 'A' && username.charAt(i) <= 'Z') ||
						(username.charAt(i) >= '0' && username.charAt(i) <= '9') 
						)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Concatenate the message in inputLine
	 * @param startingIndex: Index of the message argument
	 * @param inputLine: the client request split at white spaces
	 * @return
	 */
	private String concatenateMessage(int startingIndex, String [] inputLine) {
		String message = "";
		for (int i = startingIndex ; i < inputLine.length ; i++) {
			message += inputLine[i]+" ";
		}
		return message;
	}
}
