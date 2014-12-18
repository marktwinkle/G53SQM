
/*
 * The database part is a connection between the MySQL server (which hosts the all mail and maildrop data) and the server
 * The database object is created in the command interpreter, and does not connect to server
 * To manage releasing the lock for a user maildrop, the commandInterpreter has a method to return the instance or database used by client, and using it the server can release client's lock
 * It implements an interface, following the Strategy design pattern, to enable different implementations (i.e. another way to read mails). 
 * This implementation only holds the information of client id, and the corresponding list of mail IDs he has
 * The way it works is by using Java Database Connectivity (JDBG)
 * The Database class connects to the MySQL server using 'Connection', and interacts with it via query statements 'Statement' 
 * Query statements either modify the MySQL database or load data from it
 * The implementation can be improved by applying the Singleton design pattern
 */


/*
 * MODIFICATIONS MADE:
 * 		object 'mock' is change from a 'MockDatabase' to 'DatabaseInterface' in order to prevent a direct access to database
 * 		method validArgs(String [] inputLine) is change to validCommand(String [] inputLine) so it checks for command existence and validity at the same time
 * 		'invalid arguments' error message is replaced with the general 'invalid command' message, to reduce nested if statements
 * 		CRLF added at the end of every response to meet POP3 spec
 * 		ARGUMENT_MAX_LENGTH final variable is added to meet POP3 spec
 * 		JUnit for CommandInterpreter now runs for the new database and for modified CommandInterpreter
 * 		Termination octet is added at the end of multi-line responses to meet POP3 spec
 * 		The timeout argument is introduced to the main function in server
 * 		The timeout function has been fixed by using Socket.setSoTimeout(int)
 * 		Methods were added/removed from DatabaseInterface class for re-usability of interface (java supports multiple interfaces for a class)
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database implements DatabaseInterface {

	private final String TERMINATION_OCTET = ".";
	private final String CRLF = "\r\n";
	private final int INVALID_USER_ID = -1;
	private Connection databaseConnection;
	private Statement statement;
	private ResultSet resultSet;

	private List <Integer> mailList;
	private int loggedUserID;

	public Database() {
		loggedUserID = INVALID_USER_ID;
		mailList = new ArrayList <Integer> ();
		establishDatabaseConnection();
	}

	@Override
	public boolean user(String username) {
		try {
			loggedUserID = INVALID_USER_ID;

			String mailQuety = "Select vchUsername, iMaildropID from m_Maildrop where vchUsername = \""+ username+"\"";
			resultSet = statement.executeQuery(mailQuety);
			resultSet.next();
			loggedUserID = Integer.parseInt(resultSet.getString("iMaildropID"));
			return true;

		} catch (SQLException sqlException) {
			System.err.println("Unable to load Maildrop");
		}
		return false;
	}

	public boolean pass(String password) {
		if (loggedUserID != INVALID_USER_ID) {
			try {
				String mailQuety = "Select vchPassword, tiLocked from m_Maildrop where iMaildropID = "+ loggedUserID;
				resultSet = statement.executeQuery(mailQuety);
				resultSet.next();

				if (resultSet.getString("vchPassword").equals(password) && resultSet.getString("tiLocked").equals("0")) {
					return loadMails(loggedUserID) && getLock(loggedUserID);
				}
			} catch (IndexOutOfBoundsException indexOutOfBoundsException) {
				System.err.println("Incorrect user login");
				indexOutOfBoundsException.printStackTrace();
			} catch (SQLException sqlException) {
				sqlException.printStackTrace();
				System.err.println("Problem loading data from Database");
			}	
		}
		loggedUserID = INVALID_USER_ID;
		return false;
	}

	public boolean quit() {
		return deleteMails() && releaseLock(loggedUserID);
	}

	public String stat() {
		String response;

		response = getTotalMailsNumber()+" "+getTotalMailsSize();
		return response;
	}

	public String list() {
		String response;		
		response = getTotalMailsNumber() +" messages ("+getTotalMailsSize()+" octets)"+CRLF;

		for (int i = 0 ; i < mailList.size() ; i++) {
			if (!isDeleted(i)){
				response += (i+1)+" "+ mailSize(i)+CRLF;
			}
		}
		response += TERMINATION_OCTET;
		return response;
	}

	public String list(int mailNum) {
		String response;

		response = (mailNum+1)+" "+ mailSize(mailNum);
		return response;
	}

	public String retr(int mailNum) {
		String response;

		response = 	 mailSize(mailNum)+" octets"+CRLF+
				mailHeader(mailNum)+CRLF+
				mailBody(mailNum)+CRLF;
		response += TERMINATION_OCTET;
		return response;
	}

	public boolean dele(int mailNum) {
		try {
			String mailQuery = "Update m_Mail set markedForDeletion = 1 where iMailID = "+ mailList.get(mailNum);
			statement.executeUpdate(mailQuery);
			mailList.remove(mailNum);
			return true;
		} catch (SQLException sqlException) {
			System.err.println("Unable to mark for deletion");
		} catch (IndexOutOfBoundsException indexOutOfBoundsException) {
			System.err.println("Invalid mail number");
		}
		return false;
	}

	public void noop() {

	}

	public void rset() {
		loadMails(loggedUserID);
		int mailListSize = mailList.size();
		for ( int i = 0 ; i < mailListSize ; i ++) {
			if(isDeleted(i)) {
				unmarkDelete(i);	
			}
		}
	}

	public String top(int mailNum, int lines) {
		String response;
		String [] body;

		response = CRLF;
		response += mailHeader(mailNum);
		response += "\n\n";
		body = mailBody(mailNum).split("\n");


		if (lines > body.length) {
			response += mailBody(mailNum);
			return response;
		}
		for (int i = 0 ; i < lines ; i++) {
			response += body[i]+"\n";
		}
		response += TERMINATION_OCTET;
		return response;

	}

	public String uidl() {
		String response;

		response = CRLF;
		int totalMailsNum =  getTotalMailsNumber();
		for (int i = 0 ; i < totalMailsNum ; i++) {
			if (!isDeleted(i)){
				response += uidl(i)+CRLF;
			}
		}
		response += TERMINATION_OCTET;
		return response;

	}

	public String uidl(int mailNum) {
		String response;

		try {
			String mailQuety = "Select vchUIDL from m_Mail where iMailID = "+ mailList.get(mailNum);
			resultSet = statement.executeQuery(mailQuety);
			resultSet.next();
			response = (mailNum+1)+" "+resultSet.getString("vchUIDL");
			return response;
		} catch (SQLException sqlException) {
			System.err.println("Unable to get UIDL for message " + mailNum);
		}
		return null;
	}

	public boolean exists(int mailNum) {
		return mailNum >= 0 && mailList.size() > mailNum && !isDeleted(mailNum);
	}

	public boolean releaseLock(int userId) {
		if (loggedUserID != INVALID_USER_ID) {
			try {
				String mailQuery = "Update m_Maildrop set tiLocked = 0 where iMaildropID = "+ userId;
				statement.executeUpdate(mailQuery);
				statement.close();
				databaseConnection.close();
				return true;
			} catch (SQLException sqlException) {
				System.err.println("Can't release the user lock in maildrop");
				sqlException.printStackTrace();
			}
		}
		return false;
	}
	
	public int getLoggedUserId() {
		return loggedUserID;
	}
	
	public int getTotalMailsNumber() {
		int total = 0;
		for (int i = 0 ; i < mailList.size() ; i++) {
			if (!isDeleted(i)) {
				total ++;
			}
		}
		return total;
	}

	
	public int getTotalMailsSize() {
		int totalSize = 0;
		for (int i = 0 ; i < mailList.size() ; i++) {
			if (!isDeleted(i)) {
				totalSize += mailSize(i);
			}
		}
		return totalSize;
	}

	//start of private methods ------------------------------------------------------------
	

	/**
	 * Obtains lock for user in maildrop
	 * @param userId: the id for user logged in
	 * @return true: if lock is successfully obtained, false: otherwise
	 */
	private boolean getLock(int userId) {
		try {
			String mailQuery = "Update m_Maildrop set tiLocked = 1 where iMaildropID = "+ userId;
			statement.executeUpdate(mailQuery);
			return true;
		} catch (SQLException sqlException) {
			System.err.println("Can't obtain the user lock in maildrop");
		}
		return false;
	}

	/**
	 * Creates Connections and Statement objects to interact with the MySQL server
	 * @return true: if all connection to MySQL is successful, false: otherwise
	 */
	private boolean establishDatabaseConnection() {
		try {
			// Dynamically loads a driver class
			//TODO: change to university database
			Class.forName("com.mysql.jdbc.Driver");
			// Establishes connection to database by obtaining a Connection object
			databaseConnection = DriverManager.getConnection("jdbc:mysql://mysql.cs.nott.ac.uk", "amb12u", "monday");
			// Creates a Statement object for sending SQL statements to the database
			statement = databaseConnection.createStatement();
			return true;
		} catch (ClassNotFoundException classNotFoundException) {
			System.err.println("Problem loading driver class");
		} catch (SQLException sqlException) {
			System.err.println("Problem connecting to Database");
		}
		return false;
	}

	/**
	 * Loads the IDs of mails corresponding to specified user into a list
	 * @return true: if successfully loaded IDs into the list, false: otherwise
	 */
	private boolean loadMails(int userId) {
		try {
			mailList.clear();
			String mailQuery = "Select iMailID from m_Mail where iMaildropID = "+ userId;
			resultSet = statement.executeQuery(mailQuery);
			while(resultSet.next()) {
				mailList.add(Integer.parseInt(resultSet.getString("iMailID")));
			}
			return true;
		} catch (SQLException sqlException) {
			System.err.println("Unable to get mails");
		}
		return false;
	}

	/**
	 * Checks whether specified mail is marked for deletion or not
	 * @param mailNum: he number of selected mail
	 * @return true: if selected mail is marked for deletion, false: otherwise
	 */
	private boolean isDeleted(int mailNum) {
		try {
			String mailQuety = "Select markedForDeletion from m_Mail where iMailID = "+ mailList.get(mailNum);
			resultSet = statement.executeQuery(mailQuety);
			resultSet.next();
			return !resultSet.getString("markedForDeletion").equals("0"); 

		} catch (SQLException sqlException) {
			System.err.println("Unable to check whether mail is deleted or not");
		}
		return false;
	}

	/**
	 * Deletes all mail marked for deletion from the database
	 * @return true: if all mails has been successfully deleted, false: otherwise
	 */
	private boolean deleteMails() {
		try {
			String mailQuery = "Delete from m_Mail where iMaildropID = " + loggedUserID + " and markedForDeletion = 1";
			statement.executeUpdate(mailQuery);
			return true;
		} catch (Exception exception) {
			System.err.println("Mails weren't deleted");
		}
		return false;
	}

	/**
	 * Gets the size of specified mail in octets
	 * @param mailNum: the number of selected mail
	 * @return the size of mail selected in octets
	 */
	private int mailSize(int mailNum) {
		try {
			String mailQuety = "Select txMailContent from m_Mail where iMailID = "+ mailList.get(mailNum);
			resultSet = statement.executeQuery(mailQuety);
			resultSet.next();
			return resultSet.getString("txMailContent").length(); 
		} catch (SQLException sqlException) {
			System.err.println("Unable to check whether mail is delelter or not");
		}
		return -1;
	}

	/**
	 * Unmarks the specified mail for deletion
	 * @param mailNum: the number of selected mail
	 */
	private void unmarkDelete(int mailNum) {
		try {
			String mailQuery = "Update m_Mail set markedForDeletion = 0 where iMailID = "+ mailList.get(mailNum);
			statement.executeUpdate(mailQuery);
		} catch (SQLException sqlException) {
			System.err.println("Unable to unmark for deletion");
		}
	}

	/**
	 * Gets the header content for specified mail content
	 * @param mailNum: the number of selected mail
	 * @return multiple lines containing the header of message as String
	 */
	private String mailHeader(int mailNum) {
		try {
			String mailQuety = "Select txMailContent from m_Mail where iMailID = "+ mailList.get(mailNum);
			resultSet = statement.executeQuery(mailQuety);
			resultSet.next();
			String mailContent = resultSet.getString("txMailContent"); 
			return mailContent.substring(0, mailContent.indexOf("\n\n"));
		} catch (SQLException sqlException) {
			System.err.println("Unable to get mail header");
		}
		return null;
	}

	/**
	 * Gets the body content for specified mail content
	 * @param mailNum: the number of selected mail
	 * @return multiple lines containing the body of message as String
	 */
	private String mailBody(int mailNum) {
		try {
			String mailQuety = "Select txMailContent from m_Mail where iMailID = "+ mailList.get(mailNum);
			resultSet = statement.executeQuery(mailQuety);
			resultSet.next();
			String mailContent = resultSet.getString("txMailContent"); 
			return mailContent.substring(mailContent.indexOf("\n\n")+2);
		} catch (SQLException sqlException) {
			System.err.println("Unable to get mail body");
		}
		return null;
	}
}
