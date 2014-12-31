
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

	private List <Integer> messagesList;
	private int loggedUserID;

	private boolean testBool;
	private int testInt;

	public Database() {
		loggedUserID = INVALID_USER_ID;
		messagesList = new ArrayList <Integer> ();
		establishDatabaseConnection();
		testBool = true;
		testInt = 5;
	}


	@Override
	public boolean iden(String username) {
		try {
			//check if user name exists
			String chatQuery = "Select UserName, UserID from User where lower(UserName) = \""+username.toLowerCase()+"\"";
			resultSet = statement.executeQuery(chatQuery);
			if (resultSet.next()) {
				return false;
			}

			//add user name to database
			chatQuery = "Insert into User Values (NULL, \""+username+"\")";
			statement.executeUpdate(chatQuery);	

		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} 
		return false;
	}

	@Override
	public boolean quit() {
		// delete user from database
		String chatQuery = "Delete from User where UserID = "+loggedUserID;
		try {
			statement.executeUpdate(chatQuery);
			return true;
		} catch (SQLException e) {
			System.err.println("Unable to logout");
		}
		return false;
	}

	@Override
	public String list() {
		String list = "";
		int ctr = 1;
		
		String chatQuery = "Select * from User";
		try {
			resultSet = statement.executeQuery(chatQuery);
			list = " # \tUser Name\t# Messgaes\tStatus\n";
			while (resultSet.next()) {
				list += "\t("+ctr+")\t"+resultSet.getString("Username")+"\t"+resultSet.getString("NumMsg")+"\t";
				if (resultSet.getBoolean("Status")) {
					list += "Online\n";
				} else {
					list += "Offline\n";
				}
				ctr++;
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
		return list;
	}

	@Override
	public int getLoggedUserId() {
		return loggedUserID;
	}

	@Override
	public String stat() {
		String stats = "";
		int ctr = 1;
		
		String chatQuery = "Select UserName from User";
		try {
			resultSet = statement.executeQuery(chatQuery);
			stats = "# \tUser Name\n";
			while (resultSet.next()) {
				stats += "\t("+ctr+")\t"+resultSet.getString("Username")+"\n";
				ctr++;
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
		return stats;
	}

	@Override
	public boolean mesg(String destinationUser, String messageText) {
		String mailQuety = "Insert into DirectMessages Values (NULL, \""+loggedUserID+"\", \""+destinationUser+"\", \""+messageText+"\")"; 
		try {
			resultSet = statement.executeQuery(mailQuety);
		} catch (SQLException e) {
			System.err.println("Unable to message user");
			return false;
		}
		return true;
	}


	@Override
	public boolean hail() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String myMsgs() {
		String messages = "";
		loadMessages();
		messages = stringMessages();
		deleteMessages();
		return messages;
	}





	//start of private methods ------------------------------------------------------------

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
			databaseConnection = DriverManager.getConnection("jdbc:mysql://54.84.79.252:3306/sql562752", "sql562752", "pK6%aB7%");
			// Creates a Statement object for sending SQL statements to the database
			statement = databaseConnection.createStatement();
			return true;
		} catch (ClassNotFoundException classNotFoundException) {
			System.err.println("Problem loading driver class");
			classNotFoundException.printStackTrace();
		} catch (SQLException sqlException) {
			System.err.println("Problem connecting to Database");
		}
		return false;
	}

	/**
	 * Loads the IDs of messages corresponding to specified user into a list
	 * @return true: if successfully loaded message IDs into the list, false: otherwise
	 */
	private boolean loadMessages() {
		try {
			messagesList.clear();
			String mailQuery = "Select iMailID from m_Mail where iMaildropID = "+ loggedUserID;
			resultSet = statement.executeQuery(mailQuery);
			while(resultSet.next()) {
				messagesList.add(Integer.parseInt(resultSet.getString("iMailID")));
			}
			return true;
		} catch (SQLException sqlException) {
			System.err.println("Unable to get mails");
		}
		return false;
	}


	/**
	 * Extracts the messages from the database, and concatenates the sender and message information
	 * @return concatenated message
	 */
	private String stringMessages() {
		// TODO Auto-generated method stub
		String message = "";

		return message;
	}

	/**
	 * Deletes all logged in user messages from the database
	 * @return true: if all messages have been successfully deleted, false: otherwise
	 */
	private boolean deleteMessages() {
		try {
			String mailQuery = "Delete from m_Mail where iMaildropID = " + loggedUserID + " and markedForDeletion = 1";
			statement.executeUpdate(mailQuery);
			return true;
		} catch (Exception exception) {
			System.err.println("Mails weren't deleted");
		}
		return false;
	}
}
