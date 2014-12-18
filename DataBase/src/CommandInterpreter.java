
public class CommandInterpreter {

	private final int COMMAND = 0;
	private final int ARG_ONE = 1;
	private final int ARG_TWO = 2;
	private final int ARGUMENT_MAX_LENGTH = 40;
	//FIXME: are we implementing this , what about mail body?
	private final int RESPONSE_MAX_LENGTH = 512;
	private final int AUTHORIZATION = 0;
	private final int TRANSACTION = 1;
	private final int UPDATE = 2;
	private final String OK = "+OK ";
	private final String ERR = "-ERR ";
	private final String CRLF = "\r\n";
	private int  state;
	private DatabaseInterface database;
	
	//initialize state and mock database
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

		case UPDATE: 
			return update(input);

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

		case "USER":
			if (database.user(inputLine[ARG_ONE])) {
				response = "valid mailbox";
				return OK+response+" "+request+CRLF;
			}	
			response = inputLine[ARG_ONE]+" mailbox does not exist";
			return ERR+response+" "+request+CRLF;
		
		case "PASS":
			if (database.pass(request.substring(request.indexOf(' ')+1, request.length()))) {
				state = TRANSACTION;
				response = "welcome to mailbox";
				return OK+response+" "+request+CRLF;
			}			
			response = "incorrect password";
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
		int mailNum;
		
		if (!validCommand(request)) {
			return ERR+response+" "+request+CRLF;
		} 
		
		switch (inputLine [COMMAND]) {

		case "STAT":
		response = database.stat();
			return OK+response+CRLF;
		
		case "LIST":
			if (database.getTotalMailsNumber() != 0) {
				if (inputLine.length-1 == COMMAND) {
					response = database.list();
					return OK+response+CRLF;	
				}
				mailNum = Integer.parseInt(inputLine[ARG_ONE]);
				if (database.exists(mailNum-1)) {
					response = database.list(mailNum-1);
					return OK+response+CRLF;
				}
			}
			response = "no such message, only "+database.getTotalMailsNumber()+" messages in maildrop";
			return ERR+response+" "+request+CRLF;
		
		case "RETR":
			mailNum = Integer.parseInt(inputLine[ARG_ONE]);
			if (database.exists(mailNum-1)) {
				response = database.retr(mailNum-1);
				return OK+response+" "+request+CRLF;
			}
			response = "no such message, only "+database.getTotalMailsNumber()+" messages in maildrop";
			return ERR+response+" "+request+CRLF;
		
		case "DELE":
			mailNum = Integer.parseInt(inputLine[ARG_ONE]);
			
			if (database.exists(mailNum-1)) {
				database.dele(mailNum-1);
				response = "Message "+mailNum+" deleted";
				return OK+response+" "+request+CRLF;
			}
			response = "no such message, only "+database.getTotalMailsNumber()+" messages in maildrop";
			return ERR+response+" "+request+CRLF;
		
		case "NOOP":
			response = "";
			return OK+response+" "+request+CRLF;
		
		case "RSET":
			database.rset();
			response = "maildrop has "+database.getTotalMailsNumber()+" messages ("+database.getTotalMailsSize()+" octets)";
			return OK+response+" "+request+CRLF;
		
		case "TOP":
			if (Integer.parseInt(inputLine[ARG_TWO]) < 0) {
				return ERR+response+" "+request+CRLF;
			}
			mailNum = Integer.parseInt(inputLine[ARG_ONE]);
			int lines = Integer.parseInt(inputLine[ARG_TWO]);
			if (database.exists(mailNum-1)) {
				response = database.top(mailNum-1, lines);
				return OK+response+" "+request+CRLF;	
			}
			response = "no such message";
			return ERR+response+" "+request+CRLF;
		
		case "UIDL":
			if (database.getTotalMailsNumber() != 0){
				if (inputLine.length-1 == COMMAND) {
					response = database.uidl();
					return OK+response+CRLF;
				} 
				mailNum = Integer.parseInt(inputLine[ARG_ONE]);
				response = database.uidl(mailNum-1);
				return OK+response+CRLF;
			}
			response = "no such message, only "+database.getTotalMailsNumber()+" messages in maildrop";
			return ERR+response+" "+request+CRLF;
		
		case "QUIT":
			state = UPDATE;
			return "";
		
		default: 
			return ERR+response+" "+request+CRLF;
		}
	}

	/**
	 * update state commands
	 * @param request
	 * @return response
	 */
	private String update(String request) {
		String [] inputLine = request.split(" ");
		String response = "Invalid command";
		
		if (!validCommand(request)) {
			return ERR+response+" "+request+CRLF;
		} 
		
		switch (inputLine [COMMAND]) {
		
		case "QUIT":
			if (database.quit()) {
				response = "Quitting";
				return OK+response+" "+request+CRLF;
			}
			response = "some deleted messages not removed";
			return ERR+response+" "+request+CRLF;
		
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
		case "NOOP":
		case "RSET":
		case "QUIT":
			if (inputLine.length-1 == COMMAND) {
				return true;
			}
			return false;

		// commands with one argument
		case "USER":
		case "RETR":
		case "DELE":
			if (inputLine.length-1 == ARG_ONE && inputLine[ARG_ONE].length() <= ARGUMENT_MAX_LENGTH) {
				return true;
			}
			return false;

		// commands with two arguments
		case "TOP":
			if (inputLine.length-1 == ARG_TWO && inputLine[ARG_ONE].length() <= ARGUMENT_MAX_LENGTH && inputLine[ARG_TWO].length() <= ARGUMENT_MAX_LENGTH) {
				return true;
			}
			return false;

		// commands with optional arguments
		case "UIDL":
		case "LIST":
			if (inputLine.length-1 == COMMAND) {
				return true;
			}
			if (inputLine.length-1 == ARG_ONE && inputLine[ARG_ONE].length() <= ARGUMENT_MAX_LENGTH) {
				return true;
			}
			return false;
			
		// commands with one or more arguments (Password with spaces)
		case "PASS":
			if (inputLine.length-1 >= ARG_ONE && request.substring(request.indexOf(' ')+1).length() <= ARGUMENT_MAX_LENGTH) {
				return true;
			}
			return false;
		default: 
			return false; 
		}
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
