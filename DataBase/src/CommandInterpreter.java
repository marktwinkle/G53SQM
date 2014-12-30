
public class CommandInterpreter {

	private final int COMMAND = 0;
	private final int ARG_ONE = 1;
	private final int ARG_TWO = 2;
	private final int ARGUMENT_MAX_LENGTH = 255;
	private final int AUTHORIZATION = 0;
	private final int TRANSACTION = 1;
	private final String OK = "+OK ";
	private final String ERR = "-ERR ";
	private final String CRLF = "\r\n";
	private int  state;
	private DatabaseInterface database;
	
	//Initialise state and mock database
	public CommandInterpreter() {
		state = AUTHORIZATION;
		database = new Database();
	}

	/**
	 * 
	 * @param input: message from network
	 * @return response
	 */
	public String handleInput(String input) {
		
		String response;
		switch (state) {
		
		case AUTHORIZATION: 
			return authorization(input);

		case TRANSACTION: 
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
			return ERR+response+" "+request+CRLF;
		} 
		
		switch (inputLine [COMMAND]) {

		case "IDEN":
			if (database.iden(inputLine[ARG_ONE]) && validateUserName(inputLine[ARG_ONE])) {
				state = TRANSACTION;
				response = "valid username";
				return OK+response+" "+request+CRLF;
			}	
			response = inputLine[ARG_ONE]+" invalid username";
			return ERR+response+" "+request+CRLF;
		
		case "QUIT":
			response = "server signing off.";
			return OK+response+" "+request+CRLF;
		
		default: 
			return ERR+response+" "+request+CRLF;
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
		
		if (!validCommand(request)) {
			return ERR+response+" "+request+CRLF;
		} 
		
		switch (inputLine [COMMAND]) {

		case "STAT":
		response = database.stat();
		//TODO: Status message should say how many users are currently logged in and your current session status (logged in / not logged in) and if logged in number of messages sent. 
			return OK+response+CRLF;
		
		case "LIST":
			response = database.list();
			return OK+response+CRLF;	
			
		case "MESG":
			if (database.iden(inputLine[ARG_ONE])) { //TODO: add condition (&& database.idenStatus(username))
				//TODO: send him a message (implement send in database), response is a confirmation message
				String message = "";
				
				for (int i = ARG_ONE ; i < inputLine.length ; i++) {
					message += inputLine[i]+" ";
				}
				return OK+response+CRLF;
			}
			return ERR+response+" "+request+CRLF;

		case "HAIL":
			//TODO: send message to everybody
			String message = "";
			
			for (int i = ARG_ONE ; i < inputLine.length ; i++) {
				message += inputLine[i]+" ";
			}
			return OK+response+CRLF;
			
		case "QUIT":
			if (database.quit()) {
				response = "Quitting";
				return OK+response+" "+request+CRLF;
			}	
		default: 
			return ERR+response+" "+request+CRLF;
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
		case "HAIL":
			if (inputLine.length-1 == ARG_ONE && inputLine[ARG_ONE].length() <= ARGUMENT_MAX_LENGTH) {
				return true;
			}
			return false;

		// commands with two arguments
		case "MESG":
			if (inputLine.length-1 == ARG_TWO && inputLine[ARG_ONE].length() <= ARGUMENT_MAX_LENGTH && inputLine[ARG_TWO].length() <= ARGUMENT_MAX_LENGTH) {
				return true;
			}
			return false;

		default: 
			return false; 
		}
	}
	
	/**
	 * Checks user name for illegal characters
	 * @param username: user-input user name
	 * @return True: no invalid characters, False: invalid characters are inputed
	 */
	private boolean validateUserName(String username) {
		// TODO Auto-generated method stub
		return true;
	}
	
	public DatabaseInterface getDatabase() {
		return database;
	}
	
	// Methods for JUnitTest
	
	public int getState() {
		return state;
	}
	public boolean isValidArgs(String request) {
		return validCommand(request);
	}
	

}
