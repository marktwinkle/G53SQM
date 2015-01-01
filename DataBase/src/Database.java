
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

public class Database implements DatabaseInterface {

	private final int INVALID_USER_ID = -1;
	private Connection databaseConnection;
	private Statement statement;
	private ResultSet resultSet;

	private int loggedUserID;

	public Database() {
		loggedUserID = INVALID_USER_ID;
		establishDatabaseConnection();
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
			chatQuery = "Insert into User Values (NULL, \""+username+"\", 1, 0)";
			statement.executeUpdate(chatQuery);	
			
			chatQuery = "Select UserID from User where lower(UserName) = \""+username.toLowerCase()+"\"";
			resultSet = statement.executeQuery(chatQuery);
			resultSet.next();
			loggedUserID = resultSet.getInt("UserID");
			return true;

		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} 
		return false;
	}

	@Override
	public boolean quit() {
		// update user status to offline
		String chatQuery = "Update User Set Stat = 0 Where UserID = "+loggedUserID;
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
			list = "\n# \tUser Name\t# Messgaes\tStatus";
			while (resultSet.next()) {
				list += "\n("+ctr+")\t"+resultSet.getString("Username")+"\t\t"+resultSet.getString("NumMsg")+"\t\t";
				if (resultSet.getBoolean("Stat")) {
					list += "Online";
				} else {
					list += "Offline";
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

		String chatQuery = "Select UserName from User Where Stat = 1";
		try {
			resultSet = statement.executeQuery(chatQuery);
			stats = "\n#\tUser Name\n";
			while (resultSet.next()) {
				stats += "("+ctr+")\t"+resultSet.getString("Username")+"\n";
				ctr++;
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
		return stats;
	}

	@Override
	public boolean mesg(String destinationUser, String messageText) {
		int destID = getUserID(destinationUser);
		String mailQuety = "Insert into DirectMessages Values (NULL, \""+loggedUserID+"\", \""+destID+"\", \""+messageText+"\")"; 
		try {
			statement.executeUpdate(mailQuety);
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
		messages = loadMessages();
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
	 * Loads the messages corresponding to specified user into a string
	 * @return a string of all messages sent to user, or null of there are no messages
	 */
	private String loadMessages() {
		String messages = "";
		try {
			String mailQuery = "SELECT DirectMessages.MessageID, User.Username, DirectMessages.MessageText FROM DirectMessages"
					+ " INNER JOIN User"
					+ " ON User.UserID = DirectMessages.SenderID AND DirectMessages.ReceiverID = "+ loggedUserID;
			resultSet = statement.executeQuery(mailQuery);
			while(resultSet.next()) {
				messages += "\nPrivate Message\n"
						+ "Sender: " + resultSet.getString("User.Username");
				messages += "\n" + resultSet.getString("DirectMessages.MessageText");
			}
			return messages;
		} catch (SQLException sqlException) {
			System.err.println("Unable to get messages");
		}
		return null;
	}

	/**
	 * Deletes all logged in user messages from the database
	 * @return true: if all messages have been successfully deleted, false: otherwise
	 */
	private boolean deleteMessages() {
		try {
			String mailQuery = "Delete from DirectMessages where ReceiverID = " + loggedUserID;
			statement.executeUpdate(mailQuery);
			return true;
		} catch (Exception exception) {
			System.err.println("Problem deleting messages");
		}
		return false;
	}

	private boolean isOnline(String destinationUser) {
		String chatQuery = "Select Username from User where Username ="+destinationUser+" and Status = 1";
		try {
			resultSet = statement.executeQuery(chatQuery);
			return resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private int getUserID(String username) {
		String chatQuery = "Select UserID from User where Username ="+username;
		try {
			resultSet = statement.executeQuery(chatQuery);
			if(resultSet.next()) {
				return resultSet.getInt("UserID");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
